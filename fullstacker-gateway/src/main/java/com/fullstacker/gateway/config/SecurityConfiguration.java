package me.fullstacker.gateway.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import me.fullstacker.gateway.config.properties.ApplicationProperties;
import me.fullstacker.gateway.config.properties.ApplicationProperties.Ad;
import me.fullstacker.gateway.security.CustomActiveDirectoryLdapAuthenticationProvider;
import me.fullstacker.gateway.security.CustomDaoAuthenticationProvider;
import me.fullstacker.gateway.security.jwt.JWTConfigurer;
import me.fullstacker.gateway.security.jwt.TokenProvider;
import me.fullstacker.util.constants.AuthoritiesConstants;
import me.fullstacker.util.constants.LdapAttributeConstants;
import me.fullstacker.util.dto.UserProfile;

import lombok.AllArgsConstructor;

/**
 * 
 * @author Seshu Kandimalla <b>Email:</b> seshagirirao.ka@fullstacker.com
 *
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final ApplicationProperties appProperties;
	private final UserDetailsService userDetailsService;
	private final TokenProvider tokenProvider;
	private final CorsFilter corsFilter;
	private final SecurityProblemSupport problemSupport;

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
		.antMatchers(HttpMethod.OPTIONS, "/**")
		.antMatchers("/app/**/*.{js,html}")
		.antMatchers("/i18n/**")
		.antMatchers("/content/**")
		.antMatchers("/swagger-ui/index.html")
		.antMatchers("/test/**");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
		.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
		.exceptionHandling()
		.authenticationEntryPoint(problemSupport)
		.accessDeniedHandler(problemSupport)
		.and()
			.headers().frameOptions().disable()
		.and().sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and().authorizeRequests()
			.antMatchers("/api/register").permitAll()
			.antMatchers("/api/activate").permitAll()
			.antMatchers("/api/authenticate").permitAll()
			.antMatchers("/api/authenticateExtUser").permitAll()
			.antMatchers("/api/account/reset-password/init").permitAll()
			.antMatchers("/api/account/reset-password/finish").permitAll()
			.antMatchers("/api/buildVersion/**").permitAll()
			.antMatchers("/api/logUIError/**").permitAll()
			.antMatchers("/api/**").authenticated()
			.antMatchers("/management/health").permitAll()
			.antMatchers("/management/info").permitAll()
			.antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
		.and().apply(securityConfigurerAdapter());
	}

	private JWTConfigurer securityConfigurerAdapter() {
		return new JWTConfigurer(tokenProvider);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (appProperties.getAd().isEnabled()) {
			auth.authenticationProvider(adLdapAuthenticationProvider());
			// don't erase credentials if you plan to get them later
			// (e.g using them for another web service call)
			auth.eraseCredentials(false);
			auth.authenticationProvider(daoAuthenticationProvider());
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

		} else {
			auth.authenticationProvider(daoAuthenticationProvider());
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		}
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		CustomDaoAuthenticationProvider custAuthProvider = new CustomDaoAuthenticationProvider();
		custAuthProvider.setPasswordEncoder(passwordEncoder());
		custAuthProvider.setUserDetailsService(userDetailsService);
		return custAuthProvider;
	}

	@Bean
	public CustomActiveDirectoryLdapAuthenticationProvider adLdapAuthenticationProvider() {
		Ad ad = appProperties.getAd();
		CustomActiveDirectoryLdapAuthenticationProvider adProvider = new CustomActiveDirectoryLdapAuthenticationProvider(
				ad.getDomain(), String.format("ldap://%s:%s", ad.getHost(), ad.getPort()));
		adProvider.setUserDetailsContextMapper(userDetailsContextMapper());
		adProvider.setConvertSubErrorCodesToExceptions(true);
		adProvider.setUseAuthenticationRequestCredentials(true);

		// set pattern if it exists
		if (StringUtils.isNotBlank(ad.getSearchFilter())) {
			adProvider.setSearchFilter(ad.getSearchFilter());
		}
		return adProvider;
	}

	@Bean
	public UserDetailsContextMapper userDetailsContextMapper() {
		return new LdapUserDetailsMapper() {
			@Override
			public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
					Collection<? extends GrantedAuthority> authorities) {
				UserDetails details = super.mapUserFromContext(ctx, username, authorities);
				LdapUserDetails ldetails = (LdapUserDetails) details;
				
				Map<String, String> userAdditionalInfo = new HashMap<>();
				for (String attr : LdapAttributeConstants.getLdapAttrs()) {
					userAdditionalInfo.put(attr, ctx.getStringAttribute(attr));	
				}
				
				return new UserProfile(ldetails.getUsername(), "", ldetails.getAuthorities(),
						(LdapUserDetails) details, userAdditionalInfo);
			}
		};
	}

}
