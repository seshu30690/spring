package me.fullstacker.multipleds.component;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author SESHU
 *
 */
@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

	private static final Logger logger = LoggerFactory.getLogger(CurrentTenantIdentifierResolverImpl.class);
	
	static String DEFAULT_TENANT = "DEFAULT";
	
	@Override
	public String resolveCurrentTenantIdentifier() {
		String currentTenant = TenantContext.getCurrentTenant();
		logger.info("currentTenant: {}", currentTenant);
		return currentTenant != null ? currentTenant : DEFAULT_TENANT;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
