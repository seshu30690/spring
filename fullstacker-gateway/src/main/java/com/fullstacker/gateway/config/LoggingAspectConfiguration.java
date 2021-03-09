package me.fullstacker.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import me.fullstacker.util.aop.logging.LoggingAspect;
import me.fullstacker.util.service.AuditLogService;

import lombok.AllArgsConstructor;

/**
 * Aspect used to do Audit/event logging for all API calls
 * 
 * @author Damo
 *
 */
@Configuration
@EnableAspectJAutoProxy
@AllArgsConstructor
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoggingAspectConfiguration {
    
	private final AuditLogService auditService;
	
	@Bean	
	public LoggingAspect loggingAspect() {
		return new LoggingAspect(auditService);
	}
}
