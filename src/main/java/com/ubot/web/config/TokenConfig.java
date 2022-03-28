package com.ubot.web.config;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

// token驗證
@Provider
public class TokenConfig implements ContainerRequestFilter {

	private final Logger logger;
	private static final String SECRET = "b61DR7Rm8drVqMy3";

	public TokenConfig() {
		this.logger = LogManager.getLogger(this.getClass());
	}

	@Override
	@Produces(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
	public void filter(ContainerRequestContext request) throws IOException {

		if (!request.getUriInfo().getPath().contains("getAD")
				&& !(request.getUriInfo().getPath().contains("Log") && request.getMethod().equalsIgnoreCase("post"))) {
			String token = "";
			JSONObject result = new JSONObject();
			try {
				// Get the HTTP Authorization header from the request
				String authorizationHeader = request.getHeaderString(HttpHeaders.AUTHORIZATION);

				// Extract the token from the HTTP Authorization header
				token = authorizationHeader.substring("Bearer".length()).trim();

				// Validate the token
				Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
				logger.info("#### valid token : " + token);
				return;
			} catch (NullPointerException npe) {
				logger.error("there's no token in the headers");
				result.put("message", "無效的憑證，請重新登入");
				result.put("code", 2);
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("#### invalid token : " + token);
				result.put("message", "無效的憑證，請重新登入");
				result.put("code", 2);
			}
			request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(result.toString()).build());
			return;
		}
	}
}
