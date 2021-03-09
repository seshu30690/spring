package me.fullstacker.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrowserInfoDTO {

	private String browser;
	private String device;
	private String ip;
	private String location;

	public BrowserInfoDTO(String browser, String device, String ip, String location) {
		this.browser = browser;
		this.device = device;
		this.ip = ip;
		this.location = location;
	}
}
