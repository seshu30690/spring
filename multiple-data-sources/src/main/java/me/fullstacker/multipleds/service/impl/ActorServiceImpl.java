package me.fullstacker.multipleds.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.fullstacker.multipleds.entity.Actor;
import me.fullstacker.multipleds.repository.ActorRepository;
import me.fullstacker.multipleds.service.ActorService;

/**
 * 
 * @author SESHU
 *
 */
@Service
public class ActorServiceImpl implements ActorService {

	private static final Logger logger = LoggerFactory.getLogger(ActorServiceImpl.class);
	
	@Autowired
	private ActorRepository actorRepository;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Actor saveActor(Actor actor) {
		logger.info("Actor: {}", actor);
		return actorRepository.save(actor);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Iterable<Actor> getAll(){
		return actorRepository.findAll();
	}
	
}
