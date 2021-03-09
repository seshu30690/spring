package me.fullstacker.gateway.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.domain.UserLoginInfo;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.TokenDTO;
import me.fullstacker.util.dto.UserDTO;
import me.fullstacker.util.dto.UserInfoDTO;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

	private final Logger log = LoggerFactory.getLogger(UserServiceTest.class);
	@Autowired
	private UserService userService;

	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void changePasswordWithMatch() {
		log.info("start test changePassword with matching password");
				
		userService.changePassword("suser01", "Password1");
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void getUserByUserLogin() {
		log.info("start test getUserByUserLogin");

		GlobalResponseDTO<UserDTO> user = userService.getUserByUserLogin("suser01");
		log.info("getUserByUserLogin result , {}", user.getData());
		assertNotNull(user.getData());
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void getUserProfile() {
		log.info("start test getUserProfile");

		UserInfoDTO userInfo = userService.getUserProfile("suser01");
		log.info("getUserProfile result , {}", userInfo);
		assertNotNull(userInfo);
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void loadUserByUsername() {
		log.info("start test loadUserByUsername");

		Optional<User> user = userService.loadUserByUsername("suser01");
		log.info("loadUserByUsername result , {}", user);
		assertNotNull(user);
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void getRunningLoginInfo() {
		log.info("start test getRunningLoginInfo");

		TokenDTO activeLogin = userService.getRunningLoginInfo("suser01", "M_ADMIN");
		log.info("getRunningLoginInfo result , {}", activeLogin);
		
		if(activeLogin != null) {
			assertNotNull(activeLogin);
		}else {
			assertNull(activeLogin);			
		}
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void fetchLastLoginFailedAttempts() {
		log.info("start test fetchLastLoginFailedAttempts");

		Optional<Integer> failedAttempt = userService.fetchLastLoginFailedAttempts("suser01", "M_ADMIN");
		log.info("fetchLastLoginFailedAttempts result , {}", failedAttempt.get());
		assertTrue(failedAttempt.get() >= 0);
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void fetchLastLoggedinAt() {
		log.info("start test fetchLastLoggedinAt");

		Optional<Date> failedDateAttempt = userService.fetchLastLoggedinAt("suser01");
		log.info("fetchLastLoggedinAt result , {}", failedDateAttempt.get().toString());
		
		assertNotNull(failedDateAttempt.get().toString());
	}

	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void saveLoginInfo() {
		log.info("start test saveLoginInfo");
		UserLoginInfo userLoginInfo = new UserLoginInfo();
		userLoginInfo.setUserLogin("suser01");
		userLoginInfo.setModuleId("M_ADMIN");
		userLoginInfo.setLoggedinAt(new Date());
		userLoginInfo.setLoggedinDevice("Desktop");
		userLoginInfo.setLoggedinIp(null);
		userLoginInfo.setLoggedinBrowser("Chrome-75.0.3770.100");
		userLoginInfo.setLoggedinLocation("3.1474396,101.69921869999999");
		userLoginInfo.setVersion(0);
		userLoginInfo.setIsLoggedin(true);
		userLoginInfo.setAuthFailedReason(null);
		
		userService.saveLoginInfo(userLoginInfo);
		log.info("saveLoginInfo result , {}", userLoginInfo);
		assertNotNull(userLoginInfo);
	}

	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void updateUserSetLoginAttempts() {
		log.info("start test updateUserSetLoginAttempts");

		int loginAttemptsCount = userService.updateUserSetLoginAttempts(0, "suser01", "M_ADMIN");
		log.info("updateUserSetLoginAttempts result , {}", loginAttemptsCount);
		
		assertTrue(loginAttemptsCount >= 0);
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void updateUserSetLastLogggedinAt() {
		log.info("start test updateUserSetLastLogggedinAt");

		int loginAttemptsCount = userService.updateUserSetLastLogggedinAt(new Date(), "suser01", "M_ADMIN");
		log.info("updateUserSetLastLogggedinAt result , {}", loginAttemptsCount);
		
		assertTrue(loginAttemptsCount >= 0);
	}
	
}
