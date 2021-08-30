package com.ubot.web;

import org.glassfish.jersey.server.ResourceConfig;

import com.ubot.web.exception.MissingFileException;
import com.ubot.web.utils.Config;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/*")
public class MainApp extends ResourceConfig {

	public MainApp() {
		packages("com.ubot.web.api");
		register(Config.class);
		register(MissingFileException.class);
	}

}
