package com.ubot.web.api;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.WorkReferenceDao;
import com.ubot.web.db.vo.WorkReference;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
			message = "查詢部門別成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
			result.putPOJO("data", workReferenceList);
		} catch (Exception e) {
			message = String.format("查詢部門別失敗, 原因: %s", e.getMessage());
			logger.error(message);
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}
}
