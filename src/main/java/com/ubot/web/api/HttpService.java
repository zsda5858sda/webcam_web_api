package com.ubot.web.api;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ubot.web.db.dao.VSPValidateDao;
import com.ubot.web.db.vo.EaiVO;
import com.ubot.web.db.vo.VSPValidate;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// 接收打到AD驗證及更新人員驗證之請求
@Path("")
public class HttpService {
	private final ObjectMapper mapper;
	private final Logger logger;
	private final VSPValidateDao validateDao;

	public HttpService() {
		this.mapper = new ObjectMapper();
		this.logger = LogManager.getLogger(this.getClass());
		this.validateDao = new VSPValidateDao();
	}

	@POST
	@Path("/getAD")
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public void getAD(@Suspended final AsyncResponse asyncResponse, String requestJson)
			throws JsonMappingException, JsonProcessingException {
		EaiVO eaiVO = mapper.readValue(requestJson, EaiVO.class);
		logger.info(requestJson);

		long startTime = System.currentTimeMillis();
		final long endTime = startTime + 25000;
		Timer timer = new Timer();

		// 設定連線timeout
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				JSONObject json = new JSONObject();
				String errMessage = "AD驗證登入失敗, 原因: 連線超時";
				json.put("message", errMessage);
				json.put("code", "1");
				asyncResponse.resume(Response.status(408).entity(json.toString()).build());
			}
		}, new Date(endTime));

		Thread sendAdThread = new Thread() {
			public void run() {
				String message = "%s AD驗證登入成功";
				String errMessage = "";
				StringBuffer errMessageBuffer = new StringBuffer("AD驗證登入失敗, 原因: ");
				JSONObject result = new JSONObject();
				Response clientResponse = sendToAdHub(eaiVO);

				if (clientResponse.getStatus() == 200) {
					String responseString = clientResponse.readEntity(String.class);
					JSONObject jsonResult = new JSONObject(responseString);
					String rc2 = jsonResult.getString("rc2");
					logger.info(responseString);
					if (rc2.equals("M000")) {
						message = String.format(message,
								jsonResult.getJSONObject("result").getJSONObject("data").getString("loginID"));
						logger.info(message);
						try {
							result.put("message", message);
							result.put("code", "0");
						} catch (Exception e) {
							errMessage = errMessageBuffer.append(e.getMessage()).toString();
							logger.error(errMessage);
							setErrResult(result, errMessage);
						}

					} else {
						errMessage = errMessageBuffer.append(jsonResult.get("msg2")).toString();
						logger.error(errMessage);
						setErrResult(result, errMessage);
					}

				} else {
					errMessage = errMessageBuffer
							.append(String.format("http status code %d", clientResponse.getStatus())).toString();
					logger.error(errMessage);
					setErrResult(result, errMessage);
				}
				asyncResponse.resume(Response.status(200).entity(result.toString()).build());
			}
		};

		sendAdThread.start();

	}

	// 發送至AD之設定
	private Response sendToAdHub(EaiVO entity) {

		ClientConfig clientConfig = new ClientConfig();

		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("webrtc", "webrtc");
		clientConfig.register(feature);

		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://172.16.45.135:8080/EaiHub/resCommon/getAd01");

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.post(Entity.entity(entity, MediaType.APPLICATION_JSON));

		return response;
	}

	private void setErrResult(JSONObject result, String message) {
		logger.error(message);
		result.put("code", 1);
		result.put("message", message);
	}
}
