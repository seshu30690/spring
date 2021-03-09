package me.fullstacker.gateway.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import me.fullstacker.gateway.dto.UIErrorLogDTO;
import me.fullstacker.gateway.service.UIErrorLogService;
import me.fullstacker.util.constants.Constants;
import me.fullstacker.util.constants.ErrorCode;
import me.fullstacker.util.dto.GlobalResponseDTO;
import me.fullstacker.util.exception.CustomException;
import me.fullstacker.util.security.util.SecurityUtils;

/**
 * 
 * @author Seshu Kandimalla
 * @email seshagirirao.ka@fullstacker.com
 *
 */
@Service
public class UIErrorLogServiceImpl implements UIErrorLogService {

	private static final String UI_ADMIN_LOG = "UI-ADMIN-LOG";
	private static final String UI_CCM_LOG = "UI-CCM-LOG";
	private static final Logger ADMIN_UI_LOGGER = LoggerFactory.getLogger(UI_ADMIN_LOG);
	private static final Logger CCM_UI_LOGGER = LoggerFactory.getLogger(UI_CCM_LOG);
	private static final Logger log = LoggerFactory.getLogger(UIErrorLogServiceImpl.class);

	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final String EXEC_PREFIX = "Exception at FrontEndErrorLogServiceImpl";

	@Override
	public GlobalResponseDTO<String> generateErrLog(HttpServletRequest req, UIErrorLogDTO errLog) {
		try {
			String exID = genErrorID(errLog.getUserLogin());
			String newLn = "\n";
			StringBuilder sb = new StringBuilder(5000);
			sb.append("Error ID : " + exID);
			sb.append(newLn);
			sb.append("Client Login ID : " + SecurityUtils.getCurrentUserLogin()
					.orElseThrow(() -> new UsernameNotFoundException(Constants.WARN_NOT_LOGGED_IN)));
			sb.append(newLn);
			sb.append("Client IP Address : " + getClientAddress(req));
			sb.append(newLn);
			sb.append("Client DTM : " + dateFormat.format(errLog.getClientDTM()));
			sb.append(newLn);
			sb.append("Server DTM : " + getServerTime());
			sb.append(newLn);
			sb.append("Client Error Message : " + errLog.getErrorMessage());
			sb.append(newLn);

			String errStr = sb.toString();
			if (Constants.ADMIN_MODULE_ID.equalsIgnoreCase(errLog.getModuleId())) {
				ADMIN_UI_LOGGER.info(errStr);
			} else {
				CCM_UI_LOGGER.info(errStr);
			}

			return new GlobalResponseDTO<String>(exID);
		} catch (Exception ex) {
			log.info(EXEC_PREFIX + "::generateErrLog : {}", ex);
			throw new CustomException(ErrorCode.ERR_FETCH, ex);
		}
	}

	private String getClientAddress(HttpServletRequest req) {
		try {
			String remoteAddr = "";
			if (req != null) {
				remoteAddr = req.getHeader("X-FORWARDED-FOR");
				if (StringUtils.isBlank(remoteAddr)) {
					remoteAddr = req.getRemoteAddr();
				}
			}
			return remoteAddr;
		} catch (Exception ex) {
			log.info(EXEC_PREFIX + "::getClientAddress : {}", ex);
			return "Failed to get Client Adress info";
		}
	}

	private String getServerTime() {
		try {
			return dateFormat.format(new Date());
		} catch (Exception ex) {
			log.error(EXEC_PREFIX + "::getServerTime : {}", ex);
			return "Failed to get ServerDTM";
		}
	}

	private String genErrorID(String usrName) {
		try {
			if (usrName != null)
				return usrName + "_" + Long.toString(System.currentTimeMillis());
			return "Error 451";
		} catch (Exception ex) {
			log.info(EXEC_PREFIX + "::genErrorID : {}", ex);
			return "Error 541";
		}
	}

}
