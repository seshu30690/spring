package me.fullstacker.multipleds.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.fullstacker.multipleds.entity.User;
import me.fullstacker.multipleds.repository.UserRepository;
import me.fullstacker.multipleds.service.UserService;

/**
 * 
 * @author SESHU
 *
 */
@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(ActorServiceImpl.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public User saveUser(User user) {
		logger.info("User: {}", user);
		return userRepository.save(user);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Iterable<User> getAll(){
		return userRepository.findAll();
	}
	
}
