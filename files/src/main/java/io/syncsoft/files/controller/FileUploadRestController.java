package io.syncsoft.files.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.syncsoft.files.domain.User;
import io.syncsoft.files.properties.ApplicationProperties;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
@RestController
public class FileUploadRestController {

	private final Logger log = LoggerFactory.getLogger(FileUploadRestController.class);

	@Autowired
	private ApplicationProperties properties;

	public FileUploadRestController() {
	}

	@GetMapping("/")
	public String fileUploadApp() {
		return "Welcome to File Upload Application.";
	}

	@PostMapping(value = "/upload", produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @Valid @RequestBody User user,
			@RequestParam("id") Integer merchantId) {
		try {
			File directory = new File(properties.getFileUploadDir(), merchantId.toString());
			directory.mkdirs();
			Path writeTargetPath = Files.write(
					Paths.get(directory.getAbsolutePath(), file.getOriginalFilename()).toAbsolutePath(),
					file.getBytes(), StandardOpenOption.CREATE_NEW);
			Path fileToMovePath = Paths.get(properties.getFileUploadDir(), merchantId.toString(), "merchant_logo.png");
			Path movedPath = Files.move(writeTargetPath, fileToMovePath, StandardCopyOption.REPLACE_EXISTING);
			log.info("movedPath: {}", movedPath.toAbsolutePath());
		} catch (IOException e) {
			log.error("IOException: {}", e);
			return ResponseEntity.ok("Upload Failed'" + file.getOriginalFilename() + "'");
		}
		return ResponseEntity.ok("Upload Successfull  '" + file.getOriginalFilename() + "'");
	}

}
