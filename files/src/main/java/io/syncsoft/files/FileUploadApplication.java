package io.syncsoft.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.syncsoft.files.properties.ApplicationProperties;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class FileUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileUploadApplication.class, args);
	}

}
