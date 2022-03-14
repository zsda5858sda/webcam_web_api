package com.ubot.web.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class UnknownException extends Exception implements ExceptionMapper<UnknownException> {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final JSONObject result = new JSONObject();

	public UnknownException() {
		super("發生未知錯誤, 請聯繫管理人員");
	}

	public UnknownException(String string) {
		super(string);
	}

	@Override
	public Response toResponse(UnknownException exception) {
		String message = exception.getMessage();
		logger.error(message);
		result.put("message", message);
		result.put("code", 1);
		return Response.status(200).entity(result.toString()).type(MediaType.APPLICATION_JSON + " ;charset=UTF-8")
				.build();
	}
}