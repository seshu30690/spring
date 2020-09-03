package me.fullstacker.multipleds.config.props;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author SESHU
 *
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Map<String, DatabaseProps> datasources;

	public Map<String, DatabaseProps> getDatasources() {
		return datasources;
	}

	public void setDatasources(Map<String, DatabaseProps> datasources) {
		this.datasources = datasources;
	}

	public static class DatabaseProps {

		private String url;
		private String username;
		private String password;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

}
