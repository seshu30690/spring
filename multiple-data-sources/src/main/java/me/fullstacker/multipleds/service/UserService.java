package me.fullstacker.multipleds.service;

import me.fullstacker.multipleds.entity.User;

/**
 * 
 * @author SESHU
 *
 */
public interface UserService {

	User saveUser(User user);

	Iterable<User> getAll();

}
