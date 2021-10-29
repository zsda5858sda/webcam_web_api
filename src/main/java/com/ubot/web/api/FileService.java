package com.ubot.web.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPFileDao;
import com.ubot.web.db.vo.RequestBody;
import com.ubot.web.exception.MissingFileException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關File的所有請求
@Path("/File")
public class FileService {
	private final Logger logger;
	private final VSPFileDao fileDao;
	private final ObjectMapper mapper;
	@Context
	private HttpServletResponse response;

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
			String branch = requestBody.getBranch();
			String sql = "select * from vspfile where WORKDATE <= %s and WORKDATE >= %s ";

			if (!userId.equals("")) {
				sql += " and (FILENAME like '%s%%.webm' or FILENAME like '%s%%.jpg')";
				sql = String.format(sql, maxDate, minDate, userId, userId);
			} else {
				sql += " and (FILENAME like '%%.webm' or FILENAME like '%%.jpg')";
				sql = String.format(sql, maxDate, minDate);
			}
			if (workType.equals("ALL") || workType.equals("")) {
				// 一般行員及可全查部門
				result.putPOJO("data", fileDao.selectQuery(sql));
			}
			else {

				String sqlWorkType = "";
				// 將worktype轉成sql判斷式
				String[] workTypeArr = workType.split(";");
				for (String wt : workTypeArr) {
					sqlWorkType += String.format("WORKTYPE = %s", wt);
					if (!wt.equals(workTypeArr[workTypeArr.length - 1])) {
						sqlWorkType += " or ";
					}
				}
				sql += String.format(" and BRANCH = %s and (%s)", branch, sqlWorkType);
				result.putPOJO("data", fileDao.selectQuery(sql));
			}
			message = "檔案查詢成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			message = "檔案查詢失敗, 原因: 請聯繫管理人員";
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@GET
	@Path("/download")
	public Response downloadFile(@QueryParam("filePath") String filePath) throws MissingFileException {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new MissingFileException(filePath + " 此檔案不存在");
		}
		logger.info("檔案下載成功");
		return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
	}

	@GET
	@Path("/preview")
	public void copyToPreview(@QueryParam("filePath") String filePath) throws MissingFileException {
		logger.info(filePath);
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				throw new MissingFileException(filePath + " 此檔案不存在");
			}
			String fileName = file.getName();
			String type = fileName.substring(fileName.lastIndexOf(".") + 1);
			switch (type) {
			case "jpg":
				response.setHeader("Content-Type", "image/jpeg");
				break;
			case "webm":
				response.setHeader("Content-Type", "video/webm");
				break;
			}
			response.setHeader("Content-Length", String.valueOf(file.length()));
			response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
			Files.copy(file.toPath(), response.getOutputStream());
			logger.info("檔案預覽成功");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}
