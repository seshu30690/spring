package me.fullstacker.gateway.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import me.fullstacker.util.dto.UserProfile;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserDetailsServiceTest {

	private final Logger log = LoggerFactory.getLogger(UserDetailsServiceTest.class);
	@Autowired
	private UserDetailsService userDetailsService;

	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
		
	}
	
	@Test
	@WithMockUser(username = "suser01", password = "Password1", roles = "Admin")
	public void loadUserByUsername() {
		log.info("start get loadUserByUsername");

		String aut[] = { "Admin" };
		Collection<? extends GrantedAuthority> authorities =
		Arrays.stream(aut)
		.map(SimpleGrantedAuthority::new)
		.collect(Collectors.toList());

		UserProfile applicationUser = new UserProfile("suser01", "TEST", authorities);
		applicationUser.setLocale("EN");

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		Object user  = userDetailsService.loadUserByUsername("suser01");
		log.info("loadUserByUsername result, {}", user);
		assertNotNull(user);
	}
}
