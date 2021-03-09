package me.fullstacker.gateway.service;

import java.util.Date;
import java.util.Optional;

import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.domain.UserLoginInfo;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.TokenDTO;
import me.fullstacker.util.dto.UserDTO;
import me.fullstacker.util.dto.UserInfoDTO;

public interface UserService {

	GlobalResponseDTO<UserDTO> getUserByUserLogin(String userLogin);

	Optional<Integer> fetchLastLoginFailedAttempts(String userLogin, String moduleId);

	int updateUserSetLoginAttempts(int loginAttempts, String userLogin, String moduleId);

	void saveLoginInfo(UserLoginInfo userLoginInfo);

	public UserInfoDTO getUserProfile(String userLogin);

	void changePassword(String username, String newPassword);

	Integer updateUserSetLastLogggedinAt(Date lastLogggedinAt, String userLogin, String moduleId);

	Optional<Date> fetchLastLoggedinAt(String userLogin);

	void updateLoginInfoOnLogout(String userLogin, String moduleId);

	TokenDTO getRunningLoginInfo(String userLogin, String moduleId);

	public void releaseLocksOnLogout(String userLogin);

	public void updateTokenInfo(String token, Date tokeExpiryAt, String userLogin, String moduleId);
	
	public Optional<User> loadUserByUsername(String userLogin);

}
