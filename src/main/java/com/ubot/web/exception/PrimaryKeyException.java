package com.ubot.web.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class PrimaryKeyException extends Exception implements ExceptionMapper<PrimaryKeyException> {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final JSONObject result = new JSONObject();

	public PrimaryKeyException() {
		super("ID已存在");
	}

	public PrimaryKeyException(String message) {
		super(message);
	}

	@Override
	public Response toResponse(PrimaryKeyException exception) {
		String message = exception.getMessage();
		logger.error(message);
		result.put("message", message);
		result.put("code", 1);
		return Response.status(200).entity(result.toString()).type(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
				.build();
	}
}
