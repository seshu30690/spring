package me.fullstacker.gateway.web.rest;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.fullstacker.gateway.dto.BrowserInfoDTO;
import me.fullstacker.gateway.security.exception.NotPermittedException;
import me.fullstacker.gateway.security.jwt.HttpCookie;
import me.fullstacker.gateway.security.jwt.JWTFilter;
import me.fullstacker.gateway.security.jwt.TokenProvider;
import me.fullstacker.gateway.service.UserService;
import me.fullstacker.gateway.web.rest.vm.LoginVM;
import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.domain.UserLoginInfo;
import me.fullstacker.util.commons.DateUtils;
import me.fullstacker.util.constants.Constants;
import me.fullstacker.util.constants.DateTimeFormatConstants;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.MessageDTO;
import me.fullstacker.util.dto.TokenDTO;
import me.fullstacker.util.dto.UserInfoDTO;
import me.fullstacker.util.dto.UserProfile;
import me.fullstacker.util.enums.StatusEnum;
import me.fullstacker.util.exception.AlreadyLoggedInException;
import me.fullstacker.util.exception.CustomException;
import me.fullstacker.util.service.PreferenceUtilService;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;

/**
 * Controller to authenticate users.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthenticationJWTController {

	private final Logger log = LoggerFactory.getLogger(AuthenticationJWTController.class);

	private static final String JWT_TOKEN_COOKIE_NAME = "JWT-TOKEN";

	private final TokenProvider tokenProvider;
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final PreferenceUtilService prefService;

	@Timed
	@PostMapping("/authenticate")
	public ResponseEntity<GlobalResponseDTO<TokenDTO>> authorize(@Valid @RequestBody LoginVM loginVM,
			@RequestHeader(value = "Browser-Version", required = false) String browser,
			@RequestHeader(value = "Device", required = false) String device,
			@RequestHeader(value = "Ip", required = false) String ip,
			@RequestHeader(value = "Location", required = false) String location, HttpServletResponse response) {
		HttpHeaders httpHeaders = new HttpHeaders();
		String username = loginVM.getUsername();
		final String moduleId = loginVM.getModuleId();
		try {
			String jwt = authenticateAndGetToken(loginVM);
			username = loginVM.getUsername();
			HttpCookie.create(response, JWT_TOKEN_COOKIE_NAME, jwt, -1);
			validateUserActiveSession(loginVM);
			buildLoginInfoAndSave(new BrowserInfoDTO(browser, device, ip, location), moduleId, username, Boolean.TRUE,
					null, jwt);
			httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
			return new ResponseEntity<>(
					new GlobalResponseDTO<>(new TokenDTO(jwt, username), StatusEnum.SUCCESS, "Login Success"),
					httpHeaders, HttpStatus.OK);
		} catch (BadCredentialsException bce) {
			userService.updateUserSetLoginAttempts(1, username, moduleId);
			throw new BadCredentialsException(bce.getMessage(), bce);
		} catch (AuthenticationException ae) {
			userService.updateUserSetLoginAttempts(1, username, moduleId);
			throw new CustomException(ae.getMessage(), ae);
		} catch (AlreadyLoggedInException sae) {
			userService.updateUserSetLoginAttempts(1, username, moduleId);
			throw new AlreadyLoggedInException(sae.getMessage(), sae);
		} catch (CustomException ce) {
			userService.updateUserSetLoginAttempts(1, username, moduleId);
			throw new CustomException(ce.getMessage(), ce);
		}
	}

	@Timed
	@PostMapping("/authenticateExtUser")
	public ResponseEntity<GlobalResponseDTO<TokenDTO>> authorize(@Valid @RequestBody LoginVM loginVM,
			HttpServletResponse response) {
		HttpHeaders httpHeaders = new HttpHeaders();
		String username = loginVM.getUsername();
		final String moduleId = loginVM.getModuleId();
		try {
			String jwt = authenticateAndGetToken(loginVM);
			username = loginVM.getUsername();
			HttpCookie.create(response, JWT_TOKEN_COOKIE_NAME, jwt, -1);

			buildLoginInfoAndSave(null, moduleId, username, Boolean.TRUE, null, jwt);
			httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
			return new ResponseEntity<>(
					new GlobalResponseDTO<>(new TokenDTO(jwt, username), StatusEnum.SUCCESS, "Login Success"),
					httpHeaders, HttpStatus.OK);
		} catch (BadCredentialsException bcException) {
			log.error("BadCredentialsException: ", bcException);
			return buildLocalizedErrorMsg(httpHeaders, bcException.getMessage(), username, moduleId);
		} catch (AuthenticationException authException) {
			log.error("AuthenticationException: ", authException);
			return buildLocalizedErrorMsg(httpHeaders, authException.getMessage(), username, moduleId);
		} catch (AlreadyLoggedInException sessionActiveException) {
			log.error("AuthenticationException: ", sessionActiveException);
			return buildLocalizedErrorMsg(httpHeaders, sessionActiveException.getMessage(), username, moduleId);
		} catch (CustomException customException) {
			log.error("CustomException: ", customException);
			return buildLocalizedErrorMsg(httpHeaders, customException.getMessage(), username, moduleId);
		}
	}

	private ResponseEntity<GlobalResponseDTO<TokenDTO>> buildLocalizedErrorMsg(HttpHeaders httpHeaders, String message,
			String username, String moduleId) {
		int loginFailedAttempts = userService.updateUserSetLoginAttempts(1, username, moduleId);
		MessageDTO resMsg = new MessageDTO();
		resMsg.setMsg(message);
		resMsg.setLocalizedMsg(message);
		return new ResponseEntity<>(
				new GlobalResponseDTO<>(new TokenDTO(username, loginFailedAttempts), StatusEnum.FAILURE, resMsg),
				httpHeaders, HttpStatus.FORBIDDEN);
	}

	private String authenticateAndGetToken(LoginVM loginVM) {
		log.info("START: authenticateAndGetToken :");

		User user = userService.loadUserByUsername(loginVM.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("ID not created. Contact System Admin"));
		loginVM.setUsername(user.getUserLogin());

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginVM.getUsername(), loginVM.getPassword());
		Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
		UserProfile userProfile = (UserProfile) authentication.getPrincipal();

		// Allow login basis of Authorized Applications
		Collection<GrantedAuthority> userAuthorities = userProfile.getAuthorities();
		List<String> modules = userAuthorities.parallelStream()
				.map(auth -> StringUtils.substringBefore(auth.getAuthority(), "#")).collect(Collectors.toList());
		GrantedAuthority apps = userAuthorities.stream()
				.filter(auth -> auth.getAuthority().startsWith(loginVM.getModuleId())).findFirst()
				.orElseThrow(() -> new NotPermittedException("Not Authorized to login. Contact System Admin"));
		log.info("User Authorized to login:  {}", modules);

		userProfile.setUserGroupId(apps.getAuthority().substring(apps.getAuthority().lastIndexOf('#') + 1));
		UserInfoDTO userInfo = userService.getUserProfile(loginVM.getUsername());
		userProfile.setUserFirstName(userInfo.getUserFirstName());
		userProfile.setUserLastName(userInfo.getUserLastName());
		userProfile.setLocale(prefService.getUserPreferenceValue(loginVM.getUsername(),
				getLocalePreferenceId(loginVM.getModuleId())));
		userProfile.setModules(modules);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		boolean rememberMe = (loginVM.getRememberMe() == null) ? Boolean.FALSE : loginVM.getRememberMe();
		return tokenProvider.createToken(authentication, rememberMe);
	}

	private String getLocalePreferenceId(String moduleId) {
		if (StringUtils.isNotEmpty(moduleId)) {
			if (moduleId.equalsIgnoreCase(Constants.ADMIN_MODULE_ID)) {
				return Constants.ADMIN_LOCALE_PREF_ID;
			} else if (moduleId.equalsIgnoreCase(Constants.CCM_MODULE_ID)) {
				return Constants.CCM_LOCALE_PREF_ID;
			}
		}
		return Constants.ADMIN_LOCALE_PREF_ID;
	}

	@Timed
	@PostMapping("/logout")
	public ResponseEntity<Boolean> logoutUser(HttpServletResponse response, HttpServletRequest request,
			@Valid @RequestBody LoginVM loginVM) {
		SecurityContextHolder.clearContext();
		SecurityContextHolder.getContext().setAuthentication(null);
		userService.updateLoginInfoOnLogout(loginVM.getUsername(), loginVM.getModuleId());
		userService.releaseLocksOnLogout(loginVM.getUsername());
		HttpCookie.clear(response, JWT_TOKEN_COOKIE_NAME);
		return ResponseEntity.ok().body(true);
	}

	private void validateUserActiveSession(LoginVM loginVM) {
		TokenDTO activeTtoken = userService.getRunningLoginInfo(loginVM.getUsername(), loginVM.getModuleId());
		if (null != loginVM.getSafeKickOut() && loginVM.getSafeKickOut()) {
			if (null != activeTtoken) {
				userService.updateLoginInfoOnLogout(loginVM.getUsername(), loginVM.getModuleId());
				tokenProvider.blackListToken(loginVM.getUsername(), activeTtoken.getJwt(),
						DateUtils.getCurrentDateInString(DateTimeFormatConstants.DD_MM_YYYY_HH_MM_SS));
			}
		} else {
			if (null != activeTtoken) {
				throw new AlreadyLoggedInException("User Already Logged In");
			}
		}
	}

	private void buildLoginInfoAndSave(BrowserInfoDTO browserInfo, String moduleId, String userLogin,
			boolean isAuthSuccess, String failedReson, String jwtToken) {

		if (isAuthSuccess) {
			Optional<Date> lastLoggedinAt = userService.fetchLastLoggedinAt(userLogin);
			if (lastLoggedinAt.isPresent())
				userService.updateUserSetLastLogggedinAt(lastLoggedinAt.get(), userLogin, moduleId);
		}

		UserLoginInfo userLoginInfo = new UserLoginInfo();
		if (null != browserInfo) {
			log.info("Browser : {}", browserInfo.getBrowser());
			log.info("Device : {}", browserInfo.getDevice());
			log.info("IP : {}", browserInfo.getIp());
			log.info("Location : {}", browserInfo.getLocation());
			userLoginInfo.setLoggedinBrowser(browserInfo.getBrowser());
			userLoginInfo.setLoggedinIp(browserInfo.getIp());
			userLoginInfo.setLoggedinLocation(browserInfo.getLocation());
			userLoginInfo.setLoggedinDevice(browserInfo.getDevice());
		}

		userLoginInfo.setModuleId(moduleId);
		userLoginInfo.setLoggedinAt(new Date());
		userLoginInfo.setUserLogin(userLogin);
		userLoginInfo.setAuthSuccess(isAuthSuccess);
		userLoginInfo.setAuthFailedReason(failedReson);
		if (null != jwtToken) {
			userLoginInfo.setIsLoggedin(Boolean.TRUE);
			userService.updateTokenInfo(jwtToken, tokenProvider.getTokenExpiryDate(jwtToken), userLogin, moduleId);
		}
		userService.saveLoginInfo(userLoginInfo);
	}

}
