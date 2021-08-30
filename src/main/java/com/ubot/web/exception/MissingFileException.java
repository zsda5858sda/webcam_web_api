package com.ubot.web.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MissingFileException extends Exception implements ExceptionMapper<MissingFileException> {
	private static final long serialVersionUID = 1L;

	public MissingFileException() {
		super("此檔案不存在");
	}

	public MissingFileException(String string) {
		super(string);
	}

	@Override
	public Response toResponse(MissingFileException exception) {
		return Response.status(404).entity(exception.getMessage()).type("text/plain ;charset=UTF-8").build();
	}
}