package me.fullstacker.gateway.service;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import me.fullstacker.gateway.dto.UIErrorLogDTO;
import me.fullstacker.util.dto.GlobalResponseDTO;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UIErrorLogServiceTest {

	private final Logger log = LoggerFactory.getLogger(UIErrorLogServiceTest.class);
	@Autowired
	private UIErrorLogService uiErrorLogService;
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void generateErrLog() {
		log.info("start get generateErrLog");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("parameterName", "someValue");
		
		UIErrorLogDTO error = new UIErrorLogDTO();
		error.setModuleId("M_ADMIN");
		error.setUserLogin("suser01");
		error.setClientDTM(new Date());
		error.setErrorMessage("Unit testing error message");
		
		GlobalResponseDTO<String> errors  = uiErrorLogService.generateErrLog(request, error);
		log.info("generateErrLog result, {}", errors.getData());
		assertNotNull(errors.getData());
	}
	
}
