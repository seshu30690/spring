package me.fullstacker.gateway.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import me.fullstacker.util.admin.domain.User;
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GrantedAuthorityServiceTest {

	private final Logger log = LoggerFactory.getLogger(GrantedAuthorityServiceTest.class);
	@Autowired
	private GrantedAuthorityService grantedAuthorityService;

	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void getGrantedAuthorities() {
		log.info("start get getGrantedAuthorities");
		Set<GrantedAuthority> grantedAuthorities = grantedAuthorityService.getGrantedAuthorities("suser01");
		log.info("getGrantedAuthorities result, {}", grantedAuthorities);
		assertTrue(CollectionUtils.isNotEmpty(grantedAuthorities));
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void fetchUserByLogin() {
		log.info("start get fetchUserByLogin");
		Optional<User> user = grantedAuthorityService.fetchUserByLogin("suser01");
		log.info("fetchUserByLogin result, {}", user);
		assertNotNull(user);
	}
}
