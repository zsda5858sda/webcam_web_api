package com.ubot.web.api;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.WorkReferenceDao;
import com.ubot.web.db.vo.WorkReference;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關WorkReference的所有請求
@Path("/WorkReference")
public class WorkReferenceService {

	private final Logger logger;
	private final ObjectMapper mapper;
	private final WorkReferenceDao workReferenceDao;

	public WorkReferenceService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.mapper = new ObjectMapper();
		this.workReferenceDao = new WorkReferenceDao();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response search() throws JsonProcessingException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";

		try {
			List<WorkReference> workReferenceList = workReferenceDao.selectQuery("select * from workreference");
			message = "查詢業務種類成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
			result.putPOJO("data", workReferenceList);
		} catch (Exception e) {
			message = String.format("查詢業務種類失敗, 原因: 請聯繫管理人員");
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response save(String requestJson) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		logger.info(requestJson);
		try {
			WorkReference workReference = mapper.readValue(requestJson, WorkReference.class);
			workReferenceDao.insertQuery(workReference);
			message = "新增業務種類成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			if (e.getMessage().contains("PRIMARY")) {
				message = "新增業務種類失敗, 原因: 此ID已被註冊";
			} else {
				message = "新增業務種類失敗, 原因: 請聯繫管理人員";
			}
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@PATCH
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response update(String requestJson) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		logger.info(requestJson);
		try {
			JSONObject json = new JSONObject(requestJson);
			String sql = String.format(
					"update workreference set WORKTYPE = '%s', WORKNAME = '%s' where WORKTYPE = '%s';",
					json.get("workType"), json.get("workName"), json.get("oldKey"));
			workReferenceDao.updateQuery(sql);
			message = "更新業務種類成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			if (e.getMessage().contains("PRIMARY")) {
				message = "更新業務種類失敗, 原因: 此ID已被註冊";
			} else {
				message = "更新業務種類失敗, 原因: 請聯繫管理人員";
			}
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}
}
