package me.fullstacker.gateway.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.fullstacker.gateway.service.UserService;
import me.fullstacker.util.aop.logging.Audit;
import me.fullstacker.util.aop.logging.ExtAudit;
import me.fullstacker.util.constants.Constants;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.UserDTO;
import me.fullstacker.util.dto.UserPreferenceDTO;
import me.fullstacker.util.service.PreferenceUtilService;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserResource {

	private final Logger log = LoggerFactory.getLogger(UserResource.class);

	private final UserService userService;

	private final PreferenceUtilService prefUtilService;

	/**
	 * GET /users/:login : get the "login" user.
	 *
	 * @param login
	 *            the login of the user to find
	 * @return the ResponseEntity with status 200 (OK) and with body the "login"
	 *         user, or with status 404 (Not Found)
	 */
	@GetMapping("/users/{userLogin:" + Constants.LOGIN_REGEX + "}")
	@Timed
	public ResponseEntity<GlobalResponseDTO<UserDTO>> fetchUser(@PathVariable String userLogin) {
		log.debug("REST request to get User : {}", userLogin);
		return new ResponseEntity<>(userService.getUserByUserLogin(userLogin), HttpStatus.OK);
	}

	@ExtAudit(interfaceName="uco")
	@GetMapping("/getUserProfile/{userLogin:" + Constants.LOGIN_REGEX + "}")
	@Timed
	public ResponseEntity<GlobalResponseDTO<UserDTO>> getUserProfile(@PathVariable String userLogin) {
		log.debug("REST request to get User profile for MDM : {}", userLogin);
		return new ResponseEntity<>(userService.getUserByUserLogin(userLogin), HttpStatus.OK);
	}
	
	@Audit
	@GetMapping("/getUserPreferences/{userLogin}")
	@Timed
	public ResponseEntity<GlobalResponseDTO<List<UserPreferenceDTO>>> fetchUserPreferences(
			@PathVariable String userLogin) {
		log.debug("REST request to get User : {}", userLogin);
		GlobalResponseDTO<List<UserPreferenceDTO>> data = prefUtilService.fetchUserApplicablePrefValues(userLogin);
		return ResponseEntity.ok().body(data);
	}
}
