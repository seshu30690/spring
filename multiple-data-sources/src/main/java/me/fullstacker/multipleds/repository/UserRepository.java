package me.fullstacker.multipleds.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import me.fullstacker.multipleds.entity.User;

/**
 * 
 * @author SESHU
 *
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
