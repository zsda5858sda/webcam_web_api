package com.ubot.web.api;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPFileDao;
import com.ubot.web.db.vo.RequestBody;
import com.ubot.web.exception.MissingFileException;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/File")
public class FileService {
	private final Logger logger;
	private final VSPFileDao fileDao;
	private final ObjectMapper mapper;

	public FileService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.fileDao = new VSPFileDao();
		this.mapper = new ObjectMapper();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response search(@BeanParam RequestBody requestBody) throws IOException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate();
			String maxDate = requestBody.getMaxDate();
			String userId = requestBody.getUserId();
			String workType = requestBody.getWorkType();
			String sql = "select * from vspfile where WORKDATE <= %s and WORKDATE >= %s and (FILENAME like '%s%%-s-%%.webm' or FILENAME like '%s%%.jpg')";
			if (workType.equals("ALL")) {
				result.putPOJO("data", fileDao.selectQuery(String.format(sql, maxDate, minDate, userId, userId)));
			} else {
				sql += " and (%s)";
				String sqlWorkType = "";
				// 將worktype轉成sql判斷式
				String[] workTypeArr = workType.split(";");
				for (String wt : workTypeArr) {
					sqlWorkType += String.format("WORKTYPE = %s", wt);
					if (!wt.equals(workTypeArr[workTypeArr.length - 1])) {
						sqlWorkType += " or ";
					}
				}

				result.putPOJO("data",
						fileDao.selectQuery(String.format(sql, maxDate, minDate, userId, userId, sqlWorkType)));
			}
			message = "檔案查詢成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			e.printStackTrace();
			message = String.format("檔案查詢失敗, 原因 %s", e.getMessage());
			logger.error(message);
			result.put("message", message);
			result.put("code", 0);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}
}
