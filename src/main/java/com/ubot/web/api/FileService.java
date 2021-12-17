package com.ubot.web.api;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPFileDao;
import com.ubot.web.db.vo.RequestBody;
import com.ubot.web.db.vo.VSPFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關File的所有請求
@Path("/File")
public class FileService {
	private final Logger logger;
	private final VSPFileDao fileDao;
	private final ObjectMapper mapper;
	private final String IP = "172.16.45.245:8080";
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
			String sql = "select * from vspfile where WORKDATE <= '%s' and WORKDATE >= '%s' ";

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
			} else {

				String sqlWorkType = "";
				// 將worktype轉成sql判斷式
				String[] workTypeArr = workType.split(";");
				for (String wt : workTypeArr) {
					sqlWorkType += String.format("WORKTYPE = '%s'", wt);
					if (!wt.equals(workTypeArr[workTypeArr.length - 1])) {
						sqlWorkType += " or ";
					}
				}
				sql += String.format(" and BRANCH = '%s' and ('%s')", branch, sqlWorkType);
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
	public Response downloadFile(@QueryParam("filePath") String filePath) {
		logger.info(filePath);
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(String.format("http://%s/file_api/File/download?filePath=" + filePath, IP));
		Invocation.Builder invocaBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		return invocaBuilder.get();
	}

	@GET
	@Path("/preview")
	public Response preview(@QueryParam("filePath") String filePath) {
		logger.info(filePath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(String.format("http://%s/file_api/File/preview?filePath=" + filePath, IP));
		Invocation.Builder invocaBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		return invocaBuilder.get();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN + " ;charset=UTF-8")
	public Response uploadFile(FormDataMultiPart multipart) throws IOException {
		String workType = multipart.getField("workType").getValue();
		String cid = multipart.getField("cid").getValue();
		String uid = multipart.getField("uid").getValue();
		String branch = multipart.getField("branch").getValue();

		String message = "";
		ObjectNode result = mapper.createObjectNode();

		LocalDateTime datetime = LocalDateTime.now();
		String filePath = getFilePath(cid, uid, datetime);
		String fileName = Paths.get(filePath).getFileName().toString();
		multipart.field("filePath", filePath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(String.format("http://%s/file_api/File/upload", IP))
				.register(MultiPartFeature.class);
		logger.info("傳送至DB端");

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN + " ;charset=UTF-8");
		Response response = invocationBuilder.post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE));
		if (response.getStatus() == 200) {
			try {
				VSPFile vspFile = new VSPFile();
				vspFile.setFileName(fileName);
				vspFile.setBranch(branch);
				vspFile.setFilePath(filePath);
				vspFile.setWorkDate(datetime.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				vspFile.setWorkType(workType);

				fileDao.insertQuery(vspFile);
				message = "上傳檔案成功";
				logger.info(message);
				result.put("code", 0);
				result.put("message", message);
			} catch (Exception e) {
				message = "上傳檔案失敗, 請聯繫管理人員";
				logger.error(message);
				logger.error(e.getMessage());
				result.put("code", 1);
				result.put("message", message);
			}
		} else {
			message = response.readEntity(String.class);
			logger.error(response.getStatus());
			logger.error(message);
			result.put("code", 1);
			result.put("message", message);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	private String getFilePath(String cid, String uid, LocalDateTime datetime) {
		return "/home/petersha/dbFile/" + datetime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd/")) + uid + "/" + cid
				+ "/" + getFileName(cid, uid, datetime);
	}

	private String getFileName(String cid, String uid, LocalDateTime datetime) {
		return uid + "-" + cid + "-s-" + datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS")) + ".webm";
	}
}
