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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關Log的CRUD
@Path("/Log")
public class LogService {
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
	public Response search(@BeanParam RequestBody requestBody) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate() + "0000";
			String maxDate = requestBody.getMaxDate() + "2359";
			String userId = requestBody.getUserId();
			String sql = "select * from vsplog where CREATEDATETIME between '%s' and '%s' and USERID = %s";
			List<VSPLog> logList = logDao.searchVSPLog(String.format(sql, minDate, maxDate, userId));
			message = "查詢log成功";
			logger.info(message);
			result.putPOJO("data", logList);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			message = String.format("查詢log失敗, 原因: %s", e.getMessage());
			logger.error(message);
			result.put("message", message);
			result.put("code", 1);
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@GET
	@Path("/app")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response searchApp(@BeanParam RequestBody requestBody) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate() + "0000";
			String maxDate = requestBody.getMaxDate() + "2359";
			String userId = requestBody.getUserId();
			String sql = "select * from log where CREATEDATETIME between '%s' and '%s' and USERID = %s";
			List<Log> logList = logDao.searchAppLog(String.format(sql, minDate, maxDate, userId));
			message = "查詢log成功";
			logger.info(message);
			result.putPOJO("data", logList);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			message = String.format("查詢log失敗, 原因: %s", e.getMessage());
			logger.error(message);
			result.put("message", message);
			result.put("code", 1);
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response save(String requestJson) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		logger.info(requestJson);
		try {
			VSPLog log = mapper.readValue(requestJson, VSPLog.class);
			log.setCreateDatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS")));
			logDao.insertQuery(log);
			message = "新增log成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			message = String.format("新增log失敗, 原因: %s", e.getMessage());
			logger.error(message);
			result.put("message", message);
			result.put("code", 1);
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}
}
