package com.ubot.web.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

// 查無ID例外
@Provider
public class NotFoundException extends Exception implements ExceptionMapper<NotFoundException> {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final JSONObject result = new JSONObject();

	public NotFoundException() {
		super("查無此ID");
	}

	public NotFoundException(String string) {
		super(string);
	}

	@Override
	public Response toResponse(NotFoundException exception) {
		String message = exception.getMessage();
		logger.error(message);
		result.put("message", message);
		result.put("code", 1);
		return Response.status(200).entity(result.toString()).type(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
				.build();
	}
}
