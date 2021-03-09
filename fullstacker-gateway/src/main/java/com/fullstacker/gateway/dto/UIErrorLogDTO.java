package me.fullstacker.gateway.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UIErrorLogDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String moduleId;
	private String userLogin;
	private Date clientDTM;
	private String errorMessage;

}
