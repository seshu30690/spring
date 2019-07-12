package io.syncsoft.helloworld.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
@RestController
public class HelloWorldRestController {

	@GetMapping("/")
	public String helloWorld() {
		return "Welcome to Hello World Application.";
	}

	@GetMapping("/{name}")
	public String helloWithName(@Valid @PathVariable String name) {
		return "Hello "+name;
	}

}
