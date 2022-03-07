package com.ubot.web.config;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.ubot.web.db.dao.UserSessionDao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

// session可用性檢查
@Provider
@PreMatching
public class SessionConfig implements ContainerRequestFilter {

	@Context
	private HttpServletRequest httpRequest;
	private final UserSessionDao userSessionDao;
	private final Logger logger;

	public SessionConfig() {
		this.userSessionDao = new UserSessionDao();
		this.logger = LogManager.getLogger(this.getClass());
	}

	@Override
	public void filter(ContainerRequestContext request) throws IOException {

		// 檢查session
		if (!request.getUriInfo().getPath().equals("getAD")) {
			JSONObject result = new JSONObject();
			try {
				HttpSession session = httpRequest.getSession();
				userSessionDao.selectById(session.getId()).orElseThrow(() -> new Exception("session is not found"));
			} catch (Exception e) {
				String message = e.getMessage();
				logger.error(message);
				if (message.indexOf("session") != -1) {
					result.put("code", "2");
					result.put("message", "登入狀態逾時，請重新登入");
				} else {
					result.put("code", "1");
					result.put("message", "session檢查失敗，請聯繫管理人員");
				}
				request.abortWith(Response.status(200).entity(result.toString()).build());
				return;
			}
		}
	}
}
