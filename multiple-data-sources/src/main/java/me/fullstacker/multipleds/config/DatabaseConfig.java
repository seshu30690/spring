package me.fullstacker.multipleds.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import me.fullstacker.multipleds.component.CurrentTenantIdentifierResolverImpl;
import me.fullstacker.multipleds.component.DataSourceBasedMultiTenantConnectionProviderImpl;
import me.fullstacker.multipleds.config.props.AppProperties;
import me.fullstacker.multipleds.entity.User;

/**
 * 
 * @author SESHU
 *
 */
@Configuration
@EnableJpaRepositories(basePackages = "me.fullstacker.multipleds.repository", transactionManagerRef = "transactionManager")
@EnableTransactionManagement
@EnableConfigurationProperties({ JpaProperties.class })
public class DatabaseConfig {

	@Autowired
	private AppProperties appProps;
	
	@Autowired
	private JpaProperties jpaProperties;
	
	@Bean
	public MultiTenantConnectionProvider multiTenantConnectionProvider() {
		return new DataSourceBasedMultiTenantConnectionProviderImpl();
	}
	
	@Bean
	public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
		return new CurrentTenantIdentifierResolverImpl();
	}
	
	@Bean(name = "dataSourceMap" )
	public Map<String, DataSource> getDataSourceMap() {
		Map<String, DataSource> dataSourceMap = new HashMap<>();
		appProps.getDatasources().forEach((k, v) -> {
			dataSourceMap.put(k, DataSourceBuilder
					.create()
					.type(HikariDataSource.class)
					.url(v.getUrl())
					.username(v.getUsername())
					.password(v.getPassword())
					.build());
		});
		 return dataSourceMap;
	}
	
	@Bean
	@Primary
	@PersistenceContext
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
			MultiTenantConnectionProvider multiTenantConnectionProvider,
			CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

		Map<String, Object> hibernateProps = new LinkedHashMap<>();
		hibernateProps.putAll(this.jpaProperties.getProperties());
		hibernateProps.put(Environment.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
		hibernateProps.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
		hibernateProps.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

		// No dataSource is set to resulting entityManagerFactoryBean
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setPackagesToScan(new String[] { User.class.getPackage().getName() });
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		em.setJpaPropertyMap(hibernateProps);
		return em;
	}

	@Bean
	public EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
		return entityManagerFactoryBean.getObject();
	}

	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpa = new JpaTransactionManager();
		jpa.setEntityManagerFactory(entityManagerFactory);
		return jpa;
	}
	
}
