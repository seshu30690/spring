package me.fullstacker.multipleds;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import me.fullstacker.multipleds.component.TenantContext;
import me.fullstacker.multipleds.entity.Actor;
import me.fullstacker.multipleds.entity.User;
import me.fullstacker.multipleds.service.ActorService;
import me.fullstacker.multipleds.service.UserService;

/**
 * 
 * @author SESHU
 *
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class MultiDataSourceApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceApplication.class);
	@Autowired
	private ActorService actorService;
	@Autowired
	private UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(MultiDataSourceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("args: {}", Arrays.asList(args));
		TenantContext.setCurrentTenant(args[0]);

		if ("sakila".equalsIgnoreCase(args[0])) {
			Actor actor = new Actor();
			actor.setFirstName("Seshu");
			actor.setLastName("Kandimalla");
			actor.setLastUpdate(new Date());
			actor = actorService.saveActor(actor);
			logger.info("CommandLineRunner: Actor: {}", actor);
			Iterable<Actor> actorList = actorService.getAll();
			actorList.forEach(a->{
				logger.info("Actor ID: {}, FirstName: {}", a.getActorId(), a.getFirstName());
			});
		} else {
			User user = new User();
			user.setName("Seshu");
			user.setEmail("seshu@gmail.com");
			user = userService.saveUser(user);
			logger.info("CommandLineRunner: User: {}", user);
			Iterable<User> userList = userService.getAll();
			userList.forEach(u->{
				logger.info("User ID: {}, Name: {}", u.getUserId(), u.getName());
			});
		}

	}

}
