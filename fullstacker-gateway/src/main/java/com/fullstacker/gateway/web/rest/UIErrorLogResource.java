package me.fullstacker.gateway.web.rest;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.fullstacker.gateway.dto.UIErrorLogDTO;
import me.fullstacker.gateway.service.UIErrorLogService;
import me.fullstacker.util.dto.GlobalResponseDTO;

import lombok.AllArgsConstructor;

//REST controller for managing FrontEndErrorLogService.
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UIErrorLogResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(UIErrorLogResource.class);

	private final UIErrorLogService errLogService;

	@PostMapping("/logUIError")
	public ResponseEntity<GlobalResponseDTO<String>> logUIError(HttpServletRequest req,
			@RequestBody UIErrorLogDTO errLog) {
		if (errLog == null)
			return ResponseEntity.status(HttpStatus.OK).body(new GlobalResponseDTO<>("Error 442"));

		LOGGER.info("Logging front end exception");
		return ResponseEntity.ok().body(errLogService.generateErrLog(req, errLog));
	}

}
