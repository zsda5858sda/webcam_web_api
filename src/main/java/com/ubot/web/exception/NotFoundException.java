package com.ubot.web.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundException extends Exception implements ExceptionMapper<NotFoundException> {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LogManager.getLogger(this.getClass());

	public NotFoundException() {
		super("此查無此ID");
	}

	public NotFoundException(String string) {
		super(string);
	}

	@Override
	public Response toResponse(NotFoundException exception) {
		logger.error(exception.getMessage());
		return Response.status(404).entity(exception.getMessage()).type("text/plain ;charset=UTF-8").build();
	}
}
