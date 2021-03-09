package me.fullstacker.gateway.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.fullstacker.util.dto.BuildVersionDTO;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.dto.ReleaseVersionDTO;
import me.fullstacker.util.service.BuildVersionService;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/buildVersion")
@AllArgsConstructor
public class BuildVersionResource {

	private final Logger log = LoggerFactory.getLogger(BuildVersionResource.class);

	private final BuildVersionService buildVersionService;

	@Timed
	@GetMapping("/fetchBuildVersion/{appName}/{showDetail}")
	public ResponseEntity<GlobalResponseDTO<ReleaseVersionDTO>> fetchBuildVersion(@PathVariable String appName, @PathVariable Integer showDetail) {
		log.info("REST request to getBuildVersion");
		return ResponseEntity.ok().body(buildVersionService.getBuildVersion(appName, showDetail));
	}

	@Timed
	@PostMapping
	public ResponseEntity<GlobalResponseDTO<BuildVersionDTO>> updateBuildVersion(
			@RequestBody BuildVersionDTO buildVersionDTO) {
		log.info("REST request to updateBuildVersion");
		return ResponseEntity.ok().body(buildVersionService.saveBuildVersion(buildVersionDTO));
	}

}
