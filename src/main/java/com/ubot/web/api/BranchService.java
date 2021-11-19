package com.ubot.web.api;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPBranchDao;
import com.ubot.web.db.vo.VSPBranch;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關Branch的所有請求
@Path("/Branch")
public class BranchService {
	private final Logger logger;
	private final ObjectMapper mapper;
	private final VSPBranchDao branchDao;

	public BranchService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.mapper = new ObjectMapper();
		this.branchDao = new VSPBranchDao();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response search() throws IOException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		try {
			List<VSPBranch> branchList = branchDao.selectQuery("select * from vspbranch");
			message = "查詢分行代碼成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
			result.putPOJO("data", branchList);
		} catch (Exception e) {
			message = "查詢分行代碼失敗, 原因: 請聯繫管理人員";
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
			VSPBranch branch = mapper.readValue(requestJson, VSPBranch.class);
			branchDao.insertQuery(branch);
			message = "新增分行代碼成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			if (e.getMessage().contains("PRIMARY")) {
				message = "新增分行代碼失敗, 原因: 此ID已被註冊";
			} else {
				message = "新增分行代碼失敗, 原因: 請聯繫管理人員";
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
					"update vspbranch set BRANCHCODE = '%s', BRANCHNAME = '%s' where BRANCHCODE = '%s';",
					json.get("branchCode"), json.get("branchName"), json.get("oldKey"));
			branchDao.updateQuery(sql);
			message = "更新分行代碼成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			if (e.getMessage().contains("PRIMARY")) {
				message = "更新分行代碼失敗, 原因: 此ID已被註冊";
			} else {
				message = "更新分行代碼失敗, 原因: 請聯繫管理人員";
			}
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}
}
