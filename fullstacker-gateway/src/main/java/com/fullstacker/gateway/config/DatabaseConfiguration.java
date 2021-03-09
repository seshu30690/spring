package me.fullstacker.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import me.fullstacker.util.constants.Constants;

/**
 * 
 * @author Seshu Kandimalla
 * @email seshagirirao.ka@fullstacker.com
 *
 */
@Configuration
@EnableJpaRepositories({ Constants.PKG_REPOSITORY, Constants.PKG_UTIL_ADMIN_REPOSITORY, Constants.PKG_UTIL_ADMIN_REPOSITORY_IMPL })
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
public class DatabaseConfiguration {

   
}
