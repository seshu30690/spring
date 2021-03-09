package me.fullstacker.gateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import me.fullstacker.util.config.properties.CommonProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Properties specific to fullstacker Gateway.
 * <p>
 * Properties are configured in the application.yml file. See
 * {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties extends CommonProperties {

	private final Ad ad = new Ad();
	private final Endpoints endpoints = new Endpoints();

	public Endpoints getEndpoints() {
		return endpoints;
	}

	public Ad getAd() {
		return ad;
	}

	public static class Ad {

		private boolean enabled;
		private String domain;
		private String host;
		private int port;
		private String searchFilter;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public void setSearchFilter(String searchFilter) {
			this.searchFilter = searchFilter;
		}

	}

	@Getter
	@Setter
	public static class Endpoints {

		private String host;

	}
}
