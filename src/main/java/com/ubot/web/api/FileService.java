package com.ubot.web.api;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPFileDao;
import com.ubot.web.db.vo.RequestBody;
import com.ubot.web.db.vo.VSPFile;
import com.ubot.web.exception.UnknownException;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關File的所有請求
@Path("/File")
public class FileService {
	private final Logger logger;
	private final VSPFileDao fileDao;
	private final ObjectMapper mapper;
	// production ip
	private final String IP = "172.16.45.245:8080";
//	private final String IP = "localhost:8081";

	public FileService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.fileDao = new VSPFileDao();
		this.mapper = new ObjectMapper();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response search(@BeanParam RequestBody requestBody) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";
		try {
			String minDate = requestBody.getMinDate();
			String maxDate = requestBody.getMaxDate() + "235959";
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
				sql += String.format(" and BRANCH = '%s' and (%s)", branch, sqlWorkType);
				result.putPOJO("data", fileDao.selectQuery(sql));
			}
			message = "檔案查詢成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("檔案查詢失敗, 請聯繫管理人員");
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

	@GET
	@Path("/searchURL")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response searchURL(@BeanParam RequestBody requestBody) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		logger.info(requestBody.toString());
		String message = "";

		try {
			String userId = requestBody.getUserId();
			String customerId = requestBody.getCustomerId();
			String date = requestBody.getDate();

			String fileName = userId + "-" + customerId + "%" + date + "%";
			String sql = String.format("select * from vspfile where FILENAME like '%s'", fileName);

			List<String> urlList = new ArrayList<String>();
			fileDao.selectQuery(sql).forEach(e -> {
				String url = String.format("http://172.16.45.168:8080/webcam_web_api/api/File/preview?filePath=%s",
						e.getFilePath());
				urlList.add(url);
			});
			result.putPOJO("data", urlList);
			message = "連結查詢成功";
			logger.info(message);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("連結查詢失敗, 請聯繫管理人員");
		}
		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@GET
	@Path("/searchByDate")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response searchByDate(@BeanParam RequestBody requestBody) throws IOException, UnknownException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		logger.info(requestBody.toString());

		try {
			String date = requestBody.getDate();
			String sql = String.format(
					"select * from vspfile where WORKDATE like '%s%%' and (FILENAME like '%%.webm' or FILENAME like '%%.jpg')",
					date);
			result.putPOJO("data", fileDao.selectQuery(sql));
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new UnknownException("檔案查詢失敗, 請聯繫管理人員");
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response uploadFile(FormDataMultiPart multipart) throws IOException, UnknownException {
		String workType = multipart.getField("workType").getValue();
		String cid = multipart.getField("cid").getValue();
		String uid = multipart.getField("uid").getValue();
		String branch = multipart.getField("branch").getValue();
		List<FormDataBodyPart> imageList = multipart.getFields("image");
		FormDataBodyPart video = multipart.getField("video");
		List<Integer> fileNumList = new ArrayList<Integer>();

		int imageNum = 0, videoNum = 0;
		if (imageList == null && video != null) {
			// 影片數量(目前只有一個)
			videoNum = 1;
		} else if (video == null && imageList != null) {
			// 圖片數量
			imageNum = imageList.size();
		} else if (imageList == video) {
			throw new UnknownException("未選擇圖片或影片");
		} else {
			imageNum = imageList.size();
			videoNum = 1;
		}

		fileNumList.add(imageNum);
		fileNumList.add(videoNum);

		String message = new String();
		ObjectNode result = mapper.createObjectNode();

		LocalDateTime datetime = LocalDateTime.now();

		List<String> filePathList = getFilePath(cid, uid, datetime, fileNumList);
		filePathList.subList(0, imageNum).forEach(e -> {
			multipart.field("imagePath", e);
		});
		filePathList.subList(imageNum, imageNum + videoNum).forEach(e -> {
			multipart.field("videoPath", e);
		});

		Response videoResponse = postVideo(multipart);
		Response imageResponse = postImage(multipart);

		if (videoResponse.getStatus() == 200 && imageResponse.getStatus() == 200) {
			try {
				for (String filePath : filePathList) {
					VSPFile vspFile = new VSPFile();
					String fileName = Paths.get(filePath).getFileName().toString();
					vspFile.setFileName(fileName);
					vspFile.setBranch(branch);
					vspFile.setFilePath(filePath);
					vspFile.setWorkDate(datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
					vspFile.setWorkType(workType);

					fileDao.insertQuery(vspFile);
					message = "上傳檔案成功";
					logger.info(message);
					result.put("code", 0);
					result.put("message", message);
				}
			} catch (Exception e) {
				message = "上傳檔案失敗, 請聯繫管理人員";
				logger.error(message);
				logger.error(e.getMessage());
				result.put("code", 1);
				result.put("message", message);
			}
		} else {
			String imageResponseMessage = imageResponse.readEntity(String.class);
			String videoResponseMessage = videoResponse.readEntity(String.class);
			logger.error(imageResponse.getStatus());
			logger.error(imageResponseMessage);
			logger.error(videoResponse.getStatus());
			logger.error(videoResponseMessage);
			if (imageResponseMessage.length() >= videoResponseMessage.length()) {
				message = imageResponseMessage;
			} else {
				message = videoResponseMessage;
			}
			throw new UnknownException(message);
		}
		logger.info("傳送至DB端");

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();

	}

	private List<String> getFilePath(String cid, String uid, LocalDateTime datetime, List<Integer> fileNum) {
		List<String> filePathList = new ArrayList<String>();
		String formatDatetime = datetime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
		String fileName = "", filePath = "";
		for (int i = 1; i <= fileNum.get(0); i++) {
			fileName = uid + "-" + cid + "-" + Integer.toString(i) + ".jpg";
			filePath = "/VSP/video/" + formatDatetime + uid + "/" + cid + "/" + fileName;
			filePathList.add(filePath);
		}
		for (int i = 0; i < fileNum.get(1); i++) {
			fileName = uid + "-" + cid + "-s-" + datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
					+ ".webm";
			filePath = "/VSP/video/" + formatDatetime + uid + "/" + cid + "/" + fileName;
			filePathList.add(filePath);
		}
		return filePathList;
	}

	private Response postVideo(FormDataMultiPart multipart) {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(String.format("http://%s/file_api/File/uploadVideo", IP))
				.register(MultiPartFeature.class);

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN + " ;charset=UTF-8");
		Response response = invocationBuilder.post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE));
		return response;
	}

	private Response postImage(FormDataMultiPart multipart) {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(String.format("http://%s/file_api/File/uploadImage", IP))
				.register(MultiPartFeature.class);

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN + " ;charset=UTF-8");
		Response response = invocationBuilder.post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE));
		return response;
	}
}
