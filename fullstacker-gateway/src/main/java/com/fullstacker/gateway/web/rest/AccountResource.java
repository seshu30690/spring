package me.fullstacker.gateway.web.rest;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.fullstacker.gateway.dto.PasswordChangeDTO;
import me.fullstacker.gateway.service.UserService;
import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.repository.UserRepository;
import me.fullstacker.util.dto.UserDTO;
import me.fullstacker.util.exception.EmailAlreadyUsedException;
import me.fullstacker.util.exception.InternalServerErrorException;
import me.fullstacker.util.exception.InvalidPasswordException;
import me.fullstacker.util.security.util.SecurityUtils;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AccountResource {

	private final Logger log = LoggerFactory.getLogger(AccountResource.class);

	private final UserRepository userRepository;

	private final UserService userService;

	/**
	 * GET /authenticate : check if the user is authenticated, and return its login.
	 *
	 * @param request
	 *            the HTTP request
	 * @return the login if the user is authenticated
	 */
	@GetMapping("/authenticate")
	@Timed
	public String isAuthenticated(HttpServletRequest request) {
		log.debug("REST request to check if the current user is authenticated");
		return request.getRemoteUser();
	}

	/**
	 * 
	 * 
	 * /** POST /account : update the current user information.
	 *
	 * @param userDTO
	 *            the current user information
	 * @throws EmailAlreadyUsedException
	 *             400 (Bad Request) if the email is already used
	 * @throws RuntimeException
	 *             500 (Internal Server Error) if the user login wasn't found
	 */
	@PostMapping("/account")
	@Timed
	public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
		final String userLogin = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new InternalServerErrorException("Current user login not found"));

		Optional<User> user = userRepository.findOneByUserLogin(userLogin);
		if (!user.isPresent()) {
			throw new InternalServerErrorException("User could not be found");
		}

	}

	/**
	 * POST /account/change-password : changes the current user's password
	 *
	 * @param passwordChangeDto
	 *            current and new password
	 * @throws InvalidPasswordException
	 *             400 (Bad Request) if the new password is incorrect
	 */
	@PostMapping(path = "/account/change-password")
	@Timed
	public void updatePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
		if (!checkPasswordLength(passwordChangeDto.getNewPassword())) {
			throw new InvalidPasswordException();
		}
		userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
	}

	private static boolean checkPasswordLength(String password) {
		return !StringUtils.isEmpty(password) && password.length() >= 4 && password.length() <= 100;
	}
}
