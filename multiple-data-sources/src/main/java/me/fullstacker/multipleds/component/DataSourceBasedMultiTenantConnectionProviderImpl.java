package me.fullstacker.multipleds.component;

import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author SESHU
 *
 */
@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

	private static final long serialVersionUID = 1L;

	@Autowired
	private Map<String,DataSource> dataSourceMap;
	
	@Override
	protected DataSource selectAnyDataSource() {
		return this.dataSourceMap.values().iterator().next();
	}

	@Override
	protected DataSource selectDataSource(String tenantId) {
		return this.dataSourceMap.get(tenantId);
	}

}
