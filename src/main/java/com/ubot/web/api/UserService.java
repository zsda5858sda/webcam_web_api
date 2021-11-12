package com.ubot.web.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPUserDao;
import com.ubot.web.db.vo.VSPUser;
import com.ubot.web.exception.NotFoundException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收有關User的所有請求
@Path("/User")
public class UserService {
	private final Logger logger;
	private final ObjectMapper mapper;
	private final VSPUserDao userDao;
	private final static ConcurrentLinkedQueue<VSPUser> eventQueue = new ConcurrentLinkedQueue<VSPUser>();
	private static boolean isStart = false;

	public UserService() {
		this.logger = LogManager.getLogger(this.getClass());
		this.mapper = new ObjectMapper();
		this.userDao = new VSPUserDao();
	}

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response getById(@PathParam("userId") String userId) throws JsonProcessingException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";

		try {
			logger.info(userId);
			VSPUser user = userDao.findById(userId).orElseThrow(() -> new NotFoundException());
			message = "查詢使用者資料成功";
			logger.info(message);
			result.putPOJO("data", user);
			result.put("message", message);
			result.put("code", 0);
		} catch (NotFoundException nfe) {
			message = nfe.getMessage();
			result.put("message", message);
			result.put("code", 1);
		} catch (Exception e) {
			message = "查詢使用者資料錯誤, 原因: 請聯繫管理人員";
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@GET
	@Path("/{branch}/{dept}")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response searchUpdateList(@PathParam("branch") String branch, @PathParam("dept") String dept)
			throws IOException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";

		try {
			String sql = "select * from vspuser";
			if (!branch.matches("100|800|600") && !dept.matches("100|800|600")) {
				sql += String.format(" where BRANCH = '%s' and DEPT = '%s'", branch, dept);
			}
			List<VSPUser> userList = userDao.selectQuery(sql);
			message = "查詢使用者資料成功";
			logger.info(message);
			result.putPOJO("data", userList);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			message = "查詢使用者資料錯誤, 原因: 請聯繫管理人員";
			logger.error(message);
			logger.error(e.getMessage());
			result.put("message", message);
			result.put("code", 1);
		}

		return Response.status(200).entity(mapper.writeValueAsString(result)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public void save(@Suspended final AsyncResponse asyncResponse, String requestJson) {
		logger.info(requestJson);
		UserProcessor processor = new UserProcessor(asyncResponse, "C");
		VSPUser user;
		try {
			user = mapper.readValue(requestJson, VSPUser.class);
			eventQueue.add(user);

			while (true) {
				if (eventQueue.isEmpty()) {
					break;
				} else if (!isThreadStart()) {
					setThreadStart(true);
					processor.start();
					break;
				}

				Thread.sleep(1500);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("資料格式錯誤").build());
		} catch (InterruptedException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("執行續中斷異常").build());
		}
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public void update(@Suspended AsyncResponse asyncResponse, String requestJson) throws IOException {
		logger.info(requestJson);
		UserProcessor processor = new UserProcessor(asyncResponse, "U");
		VSPUser user;
		try {
			user = mapper.readValue(requestJson, VSPUser.class);
			eventQueue.add(user);

			while (true) {
				if (eventQueue.isEmpty()) {
					break;
				} else if (!isThreadStart()) {
					setThreadStart(true);
					processor.start();
					break;
				}

				Thread.sleep(1500);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("資料格式錯誤").build());
		} catch (InterruptedException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("執行續中斷異常").build());
		}
	}

	private static boolean isThreadStart() {
		return isStart;
	}

	private static void setThreadStart(boolean isStart) {
		UserService.isStart = isStart;
	}

	class UserProcessor extends Thread {
		private AsyncResponse asyncResponse;
		private String status;

		public UserProcessor(AsyncResponse asyncResponse, String status) {
			this.asyncResponse = asyncResponse;
			this.status = status;
		}

		@Override
		public void run() {
			synchronized (UserProcessor.class) {

				JSONObject json = new JSONObject();
				String message = status.equals("U") ? "更新使用者" : "新增使用者";
				VSPUser user = eventQueue.poll();
				boolean isHeadquarter = user.getBranch().equals(user.getDept());

				logger.info("使用者處理程序開始");

				try {
					if (isHeadquarter && user.getBranch().matches("800|100|600")) {
						userDao.insertQuery(user);
					} else {
						// 檢查主管是否已超過人數
						if (user.getManager().equals("Y")) {
							List<VSPUser> list = userDao.selectQuery(String.format(
									"select * from vspuser where DEPT = '%s' and BRANCH = '%s' and MANAGER = 'Y'",
									user.getDept(), user.getBranch()));
							if (list.size() > 0) {
								if (!list.get(0).getUserId().equals(user.getUserId())) {
									throw new Exception("manager existed");
								}
							}
						}
						// 檢查指定人員是否已超過人數
						if (user.getAppointed().equals("Y")) {
							List<VSPUser> list = userDao.selectQuery(String.format(
									"select * from vspuser where DEPT = '%s' and BRANCH = '%s' and APPOINTED = 'Y'",
									user.getDept(), user.getBranch()));
							if (list.size() > 0) {
								if (!list.get(0).getUserId().equals(user.getUserId())) {
									throw new Exception("appointed existed");
								}
							}
						}

						if (status.equals("U")) {
							userDao.updateQuery(user);
						} else {
							userDao.insertQuery(user);
						}

					}
					message += "成功";
					logger.info(message);
					json.put("message", message);
					json.put("code", 0);
				} catch (Exception e) {
					if (e.getMessage().contains("PRIMARY")) {
						message += "失敗, 原因: 此ID已被註冊";
					} else if (e.getMessage().contains("manager")) {
						message += "失敗, 原因: 該分行主管已達上限";
					} else if (e.getMessage().contains("appointed")) {
						message += "失敗, 原因: 該分行指定人員已達上限";
					} else {
						message += "失敗, 原因: 請聯繫管理人員";
					}
					logger.error(message);
					logger.error(e.getMessage());
					json.put("message", message);
					json.put("code", 1);
				}
				logger.info(user.getUserId() + " 使用者處理程序結束");

				asyncResponse.resume(Response.status(200).entity(json.toString()).build());

				setThreadStart(false);
			}
		}
	}
}
