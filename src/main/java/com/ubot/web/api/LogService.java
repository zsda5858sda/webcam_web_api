package com.ubot.web.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPLogDao;
import com.ubot.web.db.vo.Log;
import com.ubot.web.db.vo.RequestBody;
import com.ubot.web.db.vo.VSPLog;
import com.ubot.web.exception.UnknownException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關Log的所有請求
@Path("/Log")
public class LogService {
	@Context
	private HttpServletRequest httpRequest;
	private final Logger logger;
	private final ObjectMapper mapper;
	private final VSPLogDao logDao;

	public LogService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.mapper = new ObjectMapper();
		this.logDao = new VSPLogDao();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response search(@BeanParam RequestBody requestBody) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate();
			String maxDate = requestBody.getMaxDate();
			String userId = requestBody.getUserId();
			String sql = setSql("select * from vsplog", minDate, maxDate, userId);
			List<VSPLog> logList = logDao.searchVSPLog(sql);
			message = "查詢log成功";
			logger.info(message);
			result.putPOJO("data", logList);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("查詢log失敗, 請聯繫管理人員");
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@GET
	@Path("/app")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response searchApp(@BeanParam RequestBody requestBody) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate();
			String maxDate = requestBody.getMaxDate();
			String userId = requestBody.getUserId();
			String sql = setSql("select * from log", minDate, maxDate, userId);
			List<Log> logList = logDao.searchAppLog(String.format(sql, minDate, maxDate, userId));
			message = "查詢log成功";
			logger.info(message);
			result.putPOJO("data", logList);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("查詢log失敗, 請聯繫管理人員");
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response save(String requestJson) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		logger.info(requestJson);
		try {
			VSPLog log = mapper.readValue(requestJson, VSPLog.class);
			log.setIp(httpRequest.getRemoteAddr());
			log.setCreateDatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS")));
			logDao.insertQuery(log);
			message = "新增log成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("新增log失敗, 請聯繫管理人員");
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	private String setSql(String sql, String minDate, String maxDate, String userId) {
		if (minDate != null && !minDate.equals("") && minDate != null && !minDate.equals("")) {
			minDate += "0000";
			maxDate += "2359";
			sql += String.format(" where CREATEDATETIME between '%s' and '%s'", minDate, maxDate);
		}
		if (userId != null && !userId.equals("")) {
			if (sql.contains("where")) {
				sql += String.format(" and USERID = '%s'", userId);
			} else {
				sql += String.format(" where USERID = '%s'", userId);
			}
		}
		return sql;
	}
}
