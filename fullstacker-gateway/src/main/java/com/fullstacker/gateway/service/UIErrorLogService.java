package me.fullstacker.gateway.service;

import javax.servlet.http.HttpServletRequest;

import me.fullstacker.gateway.dto.UIErrorLogDTO;
import me.fullstacker.util.dto.GlobalResponseDTO;

public interface UIErrorLogService {

	public GlobalResponseDTO<String> generateErrLog(HttpServletRequest req, UIErrorLogDTO errLog);

}
