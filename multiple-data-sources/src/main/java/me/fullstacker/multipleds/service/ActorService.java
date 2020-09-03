package me.fullstacker.multipleds.service;

import me.fullstacker.multipleds.entity.Actor;

/**
 * 
 * @author SESHU
 *
 */
public interface ActorService {

	Actor saveActor(Actor user);

	Iterable<Actor> getAll();

}
