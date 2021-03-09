package me.fullstacker.gateway.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hazelcast.core.IMap;
import me.fullstacker.gateway.config.properties.ApplicationProperties;
import me.fullstacker.util.constants.Constants;
import me.fullstacker.util.dto.TokenDTO;
import me.fullstacker.util.dto.UserProfile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private final CacheManager cacheManager;
    
    private static final String AUTHORITIES_KEY = "auth";

    private Key key;

    private long tokenValidityInMilliseconds;

    private long tokenValidityInMillisecondsForRememberMe;

	private final ApplicationProperties properties;

	public TokenProvider(ApplicationProperties properties, CacheManager cacheManager) {
		this.properties = properties;
		this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes;
		String secret = properties.getSecurity().getAuthentication().getJwt().getSecret();
        if (!StringUtils.isEmpty(secret)) {
            log.warn("Warning: the JWT key used is not Base64-encoded. " +
                "We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security.");
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        } else {
            log.debug("Using a Base64-encoded JWT secret key");
			keyBytes = Decoders.BASE64.decode(properties.getSecurity().getAuthentication().getJwt().getBase64Secret());
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds =
				1000 * properties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe =
				1000 * properties.getSecurity().getAuthentication().getJwt()
                .getTokenValidityInSecondsForRememberMe();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        return Jwts.builder()
            .setSubject(authentication.getName())
				.setIssuedAt(new Date())
            .claim(AUTHORITIES_KEY, authorities)
            .claim("principal", authentication.getPrincipal())
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .parseClaimsJws(token)
            .getBody();

        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        Map prin = (HashMap) claims.get("principal");
        UserProfile profile = new UserProfile((String)prin.get("username"), "", authorities);
        profile.setUserLogin((String)prin.get("userLogin"));
        profile.setUserFirstName((String)prin.get("userLogin"));
        profile.setUserLastName((String)prin.get("userLastName"));
        profile.setUserGroupId((String)prin.get("userGroupId"));        
        profile.setLocale((String)prin.get("locale"));
        profile.setModules((List<String>)((Object)prin.get("modules")));
        return new UsernamePasswordAuthenticationToken(profile, token, authorities);
    }

	public boolean validateToken(String authToken) {
		try {
			// Allow token only if token provided is not in blacklist
			filterBlackListedTokens(authToken);
			Jws<Claims> jwsClims = Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
			jwsClims.getBody().getSubject();
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("Invalid JWT signature.");
			log.trace("Invalid JWT signature trace: {}", e);
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT token.");
			log.trace("Expired JWT token trace: {}", e);
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT token.");
			log.trace("Unsupported JWT token trace: {}", e);
		} catch (IllegalArgumentException e) {
			log.info("JWT token compact of handler are invalid.");
			log.trace("JWT token compact of handler are invalid trace: {}", e);
		}
		return false;
	}

	public String getSubject(HttpServletRequest httpServletRequest, String jwtTokenCookieName) {
		String token = HttpCookie.getValue(httpServletRequest, jwtTokenCookieName);
		if (token == null)
			return null;
		return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
	}
	
	public Date getTokenExpiryDate(String jwtToken) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken).getBody().getExpiration();
	}
	
	public String getTokenUserLogin(String jwtToken) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken).getBody().getSubject();
	}
	
	@Cacheable(value = Constants.CACHE_JWT_BLACKLISTED_TOKENS_KEY, key = "#userLogin.concat('-').concat(#blacklistingTime)", sync = true)
	public TokenDTO blackListToken(String userLogin, String jwtToken, String blacklistingTime) {
		return new TokenDTO(jwtToken, new Date(), getTokenExpiryDate(jwtToken));
	}
	
	@SuppressWarnings("unchecked")
	public void filterBlackListedTokens(String authToken) {
		Object nativeCache = cacheManager.getCache(Constants.CACHE_JWT_BLACKLISTED_TOKENS_KEY).getNativeCache();
		String userLogin = getTokenUserLogin(authToken);
		
		IMap<String, TokenDTO> map = (IMap<String, TokenDTO>) nativeCache;
		Iterator<Map.Entry<String, TokenDTO>> entryIt = map.entrySet().iterator();
		while (entryIt.hasNext()) {
			Entry<String, TokenDTO> entry = entryIt.next();
			if (entry.getKey().startsWith(userLogin)) {
				TokenDTO token = entry.getValue();
				if (null != token && token.getJwt().equals(authToken)) {
					Jws<Claims> jwClaims = Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
					throw new ExpiredJwtException(jwClaims.getHeader(), jwClaims.getBody(), "JWT expired");
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Scheduled(cron = "${cron.init.clearcache.expiretokens}")
	public void cleanExpiredBlackListedTokens() {
		log.info("Cron job Started to cleanExpiredBlackListedTokens ::");

		Object nativeCache = cacheManager.getCache(Constants.CACHE_JWT_BLACKLISTED_TOKENS_KEY).getNativeCache();
		IMap<String, TokenDTO> map = (IMap<String, TokenDTO>) nativeCache;
		Iterator<Map.Entry<String, TokenDTO>> entryIt = map.entrySet().iterator();
		while (entryIt.hasNext()) {
			Entry<String, TokenDTO> entry = entryIt.next();
			if (entry.getValue().getExpireAt().before(new Date())) {
				log.debug("Removed from cache : {}", entry.getKey());
				map.remove(entry.getKey());
			}
		}

		log.info("Cron job run completed");
	}
}
