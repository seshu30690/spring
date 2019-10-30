package io.syncsoft.files.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private String fileUploadDir;

	public String getFileUploadDir() {
		return fileUploadDir;
	}

	public void setFileUploadDir(String fileUploadDir) {
		this.fileUploadDir = fileUploadDir;
	}

}
