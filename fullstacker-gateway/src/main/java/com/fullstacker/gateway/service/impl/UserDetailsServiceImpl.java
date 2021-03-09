package me.fullstacker.gateway.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.fullstacker.gateway.security.exception.UserNotActivatedException;
import me.fullstacker.util.admin.domain.User;
import me.fullstacker.util.admin.mapper.UserGroupMapper;
import me.fullstacker.util.admin.repository.UserRepository;
import me.fullstacker.util.dto.UserGroupInfoDTO;
import me.fullstacker.util.dto.UserProfile;

import lombok.AllArgsConstructor;

/**
 * Authenticate a user with database user
 * 
 * @author Seshu Kandimalla
 *
 */
@Service("userDetailsService")
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	private final UserRepository userRepository;
	private final UserGroupMapper userGroupMapper;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(final String login) {
		log.debug("Authenticating {}", login);

		userRepository.findOneByUserLogin(login);
		return userRepository.findOneByUserLogin(login).map(user -> createSpringSecurityUser(login, user))
				.orElseThrow(() -> new UsernameNotFoundException("ID not created. Contact System Admin"));
	}

	private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {
		if (null == user || user.getStatus() != 1)
			throw new UserNotActivatedException("ID not authorized. Contact System Admin");

		return new UserProfile(user.getUserLogin(), user.getPassword(), getGrantedAuthorities(user));
	}

	private Set<GrantedAuthority> getGrantedAuthorities(User user) {
		Set<UserGroupInfoDTO> ugInfoDTOs = userGroupMapper.userGroupsToUserGroupInfoDTOs(user.getUserGroups());
		return new HashSet<>(ugInfoDTOs.stream().filter(UserGroupInfoDTO::getActive)
				.map(ugInfo -> new SimpleGrantedAuthority((new StringBuilder()).append(ugInfo.getModuleId())
						.append("#").append(ugInfo.getRoleId()).append("#").append(ugInfo.getUserGroupId()).toString()))
				.collect(Collectors.toList()));
	}

}
