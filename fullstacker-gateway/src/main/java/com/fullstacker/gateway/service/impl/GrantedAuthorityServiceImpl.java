package me.fullstacker.gateway.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.fullstacker.gateway.service.GrantedAuthorityService;
import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.repository.UserRepository;

import lombok.AllArgsConstructor;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
@Service
@AllArgsConstructor
public class GrantedAuthorityServiceImpl implements GrantedAuthorityService {

	private final UserRepository userRepository;

	@Override
	// @Transactional(readOnly = true)
	public Set<GrantedAuthority> getGrantedAuthorities(String lowercaseLogin) {
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		grantedAuthorities.addAll(loadUserAuthorities(lowercaseLogin));
		grantedAuthorities.addAll(loadGroupAuthorities(lowercaseLogin));
		return grantedAuthorities;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> fetchUserByLogin(String userLogin) {
		return userRepository.findOneByUserLogin(userLogin);
	}

	protected List<GrantedAuthority> loadUserAuthorities(String username) {
		return getGrantedAuthorities(userRepository.findUserAuthoritiesByUserLogin(username));
	}

	protected List<GrantedAuthority> loadGroupAuthorities(String username) {
		return getGrantedAuthorities(userRepository.findUserGroupAuthoritiesByUserLogin(username));
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> authorities) {
		return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

}
