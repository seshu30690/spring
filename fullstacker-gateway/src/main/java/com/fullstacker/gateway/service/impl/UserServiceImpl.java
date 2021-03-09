package me.fullstacker.gateway.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.fullstacker.gateway.service.UserService;
import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.domain.UserLoginInfo;
import me.fullstacker.util.admin.mapper.UserInfoMapper;
import me.fullstacker.util.admin.mapper.UserMapper;
import me.fullstacker.util.admin.repository.MakerCheckerRepository;
import me.fullstacker.util.admin.repository.UserAuthAppRepository;
import me.fullstacker.util.admin.repository.UserLoginInfoRepository;
import me.fullstacker.util.admin.repository.UserRepository;
import me.fullstacker.util.constants.ErrorCode;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.TokenDTO;
import me.fullstacker.util.dto.UserDTO;
import me.fullstacker.util.dto.UserInfoDTO;
import me.fullstacker.util.exception.CustomException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final UserInfoMapper userInfoMapper;
	private final UserLoginInfoRepository loginInfoRepo;
	private final UserAuthAppRepository userAuthAppRepo;
	private final PasswordEncoder passwordEncoder;
	private final MakerCheckerRepository makerCheckerRepo;

	@Async
	@Override
	public void changePassword(String username, String newPassword) {
		Optional.of(username).flatMap(userRepository::findOneByUserLogin).ifPresent(user -> {
			String currentEncryptedPassword = user.getPassword();
			if (!passwordEncoder.matches(newPassword, currentEncryptedPassword)) {
				String encryptedPassword = passwordEncoder.encode(newPassword);
				user.setPassword(encryptedPassword);
				userRepository.save(user);
				log.debug("Changed password for : {}", username);
			}
		});
	}

	@Override
	@Transactional(readOnly = true)
	public GlobalResponseDTO<UserDTO> getUserByUserLogin(String userLogin) {
		try {
			Optional<User> optUser = userRepository.findOneByUserLogin(userLogin);
			return new GlobalResponseDTO<>(userMapper.userToUserDTO(optUser.orElse(null)));
		} catch (Exception e) {
			throw new CustomException(ErrorCode.ERR_FETCH, e);
		}
	}

	@Override
	@Transactional
	public Optional<User> loadUserByUsername(String userLogin) {
		log.debug("Authenticating {}", userLogin);

		return userRepository.findOne(new Specification<User>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(
						criteriaBuilder.equal(criteriaBuilder.upper(root.get("userLogin")), userLogin.toUpperCase()));
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});
	}
	
	@Async
	@Override
	@Transactional(readOnly = false)
	public void saveLoginInfo(UserLoginInfo userLoginInfo) {
		loginInfoRepo.save(userLoginInfo);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Integer> fetchLastLoginFailedAttempts(String userLogin, String moduleId) {
		return userAuthAppRepo.fetchLastLoginAttempts(userLogin, moduleId);
	}

	@Override
	@Transactional(readOnly = false)
	public void updateTokenInfo(String token, Date tokeExpiryAt, String userLogin, String moduleId) {
		userAuthAppRepo.updateTokenInfoOnLogin(token, tokeExpiryAt, userLogin, moduleId);
	}

	@Override
	@Transactional(readOnly = false)
	public int updateUserSetLoginAttempts(int loginAttempts, String userLogin, String moduleId) {

		Optional<Integer> loginAttemptsCount = userAuthAppRepo.fetchLastLoginAttempts(userLogin, moduleId);
		if (loginAttempts > 0)
			loginAttempts = loginAttemptsCount.orElse(0).intValue() + loginAttempts;
		
		Integer updated = userAuthAppRepo.updateUserSetLoginAttempts(Integer.valueOf(loginAttempts), userLogin,
				moduleId);
		log.info("Updated Login Attempts Rows: {}", updated);
		return loginAttempts;
	}

	@Override
	@Transactional(readOnly = false)
	public Integer updateUserSetLastLogggedinAt(Date lastLogggedinAt, String userLogin, String moduleId) {
		Integer updated = userAuthAppRepo.updateUserSetLastLoggedinAt(lastLogggedinAt, userLogin, moduleId);
		log.info("Updated Last Logggedin At: {}", updated);
		return updated;
	}

	@Override
	@Transactional(readOnly = true)
	public UserInfoDTO getUserProfile(String userLogin) {
		log.info("Start getUserProfile : userLogin : {}", userLogin);
		return userInfoMapper.userInfoToUserInfoDTO(userRepository.findUserInfo(userLogin));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Date> fetchLastLoggedinAt(String userLogin) {
		log.info("START: fetchLastLoggedinAt -> userLogin : {}", userLogin);
		return loginInfoRepo.fetchLastLoggedinAt(userLogin);
	}

	@Override
	@Transactional(readOnly = false)
	public void updateLoginInfoOnLogout(String userLogin, String moduleId) {
		log.info("START: updateLoginInfoOnLogout :: userLogin : {}, moduleId: {}", userLogin, moduleId);
		loginInfoRepo.updateInfoOnLogout(new Date(), userLogin, moduleId);
		userAuthAppRepo.updateTokenInfoOnLogout(userLogin, moduleId);
	}

	@Override
	public TokenDTO getRunningLoginInfo(String userLogin, String moduleId) {
		log.info("START: getRunningLoginInfo :: userLogin : {} : moduleId : {}", userLogin, moduleId);
		List<TokenDTO> activeLogins = userAuthAppRepo.getRunningLoginInfo(userLogin, moduleId, new Date());
		if (null != activeLogins && !activeLogins.isEmpty())
			return activeLogins.get(0);
		return null;
	}

	@Override
	@Transactional(readOnly = false)
	public void releaseLocksOnLogout(String userLogin) {
		makerCheckerRepo.releaseAllLocksOnUserLogout(userLogin);
	}

}
