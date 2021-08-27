package com.ubot.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPUserDao;
import com.ubot.web.db.vo.VSPUser;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public Response getById(String requestJson) throws JsonProcessingException {
		ObjectNode result = mapper.createObjectNode();
		String message = "";
		try {
			VSPUser inputUser = mapper.readValue(requestJson, VSPUser.class);
			VSPUser user = userDao.findById(inputUser.getUserId()).orElseThrow(() -> new Exception("此ID尚未註冊"));
			message = "查詢使用者資料成功";
			logger.info(message);
			result.putPOJO("data", user);
			result.put("message", message);
			result.put("code", 0);
		} catch (Exception e) {
			e.printStackTrace();
			message = String.format("查詢使用者資料錯誤, 原因: %s", e.getMessage());
			logger.error(message);
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
		CreateUserProcessor processor = new CreateUserProcessor(asyncResponse);
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

				Thread.sleep(500);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("資料格式錯誤").build());
		} catch (InterruptedException e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(200).entity("執行續中斷異常").build());
		}
	}

	// 撈出同部門同分行之行員並寫進subordinate中
	private void setSubordinateByManager(VSPUser user, StringBuilder builder, String workType) throws Exception {
		String sql = "select * from vspuser where BRANCH = %s and (%s)";
		userDao.selectQuery(String.format(sql, user.getBranch(), workType)).stream()
				.forEach(u -> builder.append(u.getUserId().concat(";")));
		user.setSubordinate(builder.toString());
	}

	// 撈出同部門之行員並寫進subordinate中
	private void setSubordinateByHeadquarter(VSPUser user, StringBuilder builder, String workType) throws Exception {
		String sql = "select * from vspuser where %s";
		userDao.selectQuery(String.format(sql, workType)).stream()
				.forEach(u -> builder.append(u.getUserId().concat(";")));
		user.setSubordinate(builder.toString());
	}

	private static boolean isThreadStart() {
		return isStart;
	}

	private static void setThreadStart(boolean isStart) {
		UserService.isStart = isStart;
	}

	class CreateUserProcessor extends Thread {
		private AsyncResponse asyncResponse;

		public CreateUserProcessor(AsyncResponse asyncResponse) {
			this.asyncResponse = asyncResponse;
		}

		@Override
		public void run() {
			synchronized (CreateUserProcessor.class) {

				logger.info("開始新增使用者");

				JSONObject json = new JSONObject();
				VSPUser user = eventQueue.poll();
				String newUserId = user.getUserId().concat(";");
				user.setSubordinate(newUserId);
				List<VSPUser> userList = new ArrayList<VSPUser>();
				StringBuilder subordinate = new StringBuilder(newUserId);
				String message = "";
				try {
					String sql, sqlWorkType = "";
					// 將worktype轉成sql判斷式
					String[] workTypeArr = user.getWorkType().split(";");
					for (String wt : workTypeArr) {
						sqlWorkType += String.format("WORKTYPE like '%%%s%%'", wt);
						if (!wt.equals(workTypeArr[workTypeArr.length - 1])) {
							sqlWorkType += " or ";
						}
					}

					// 設定總行或主管之下屬
					if ((user.getBranch().equals(user.getDept()) && !user.getBranch().matches("0(.*)"))
							|| user.getBranch().matches("800|100")) {
						setSubordinateByHeadquarter(user, subordinate, sqlWorkType);
					} else if (user.getManager().equalsIgnoreCase("y")) {
						setSubordinateByManager(user, subordinate, sqlWorkType);
					}

					userDao.insertQuery(user);

					// 查詢需要更新名單
					sql = "select * from vspuser where USERID <> %s and (DEPT = 800 or DEPT = 100 or ((%s) and (DEPT = BRANCH and BRANCH not like '0%%' or BRANCH = %s and MANAGER = 'Y')));";

					userList = userDao.selectQuery(String.format(sql, user.getUserId(), sqlWorkType, user.getBranch()))
							.stream().map(u -> {
								u.setSubordinate(u.getSubordinate() + newUserId);
								return u;
							}).collect(Collectors.toList());
					userDao.updateSubordinate(userList);
					logger.info("更新可查看之下屬");

					message = "新增使用者成功";
					logger.info(message);
					json.put("message", message);
					json.put("code", 0);
				} catch (Exception e) {
					message = "新增使用者失敗, 原因: " + e.getMessage();
					logger.error(message);
					json.put("message", message);
					json.put("code", 1);
				}

				asyncResponse.resume(Response.status(200).entity(json.toString()).build());

				logger.info(user.getUserId() + " 使用者新增完畢");
				setThreadStart(false);
			}
		}
	}
}
