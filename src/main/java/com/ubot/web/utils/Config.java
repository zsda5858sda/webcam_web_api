package com.ubot.web.utils;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

// 在http request進來後第一個進來的class，之後才會pass到service
@Provider
@PreMatching
public class Config implements ContainerResponseFilter {

	public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
		// 設定可使用來源
		res.getHeaders().add("Access-Control-Allow-Origin", "*");
		res.getHeaders().add("Access-Control-Allow-Credentials", "true");
		res.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		res.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
	}

}