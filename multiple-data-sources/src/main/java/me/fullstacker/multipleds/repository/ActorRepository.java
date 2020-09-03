package me.fullstacker.multipleds.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import me.fullstacker.multipleds.entity.Actor;

/**
 * 
 * @author SESHU
 *
 */
@Repository
public interface ActorRepository extends CrudRepository<Actor, Long> {

}
