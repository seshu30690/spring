package me.fullstacker.gateway.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import me.fullstacker.util.admin.domain.User;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
public interface GrantedAuthorityService {

	Set<GrantedAuthority> getGrantedAuthorities(String lowercaseLogin);

	Optional<User> fetchUserByLogin(String userLogin);

}
