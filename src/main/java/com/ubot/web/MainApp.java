package com.ubot.web;

import org.glassfish.jersey.server.ResourceConfig;

import com.ubot.web.exception.MissingFileException;
import com.ubot.web.exception.NotFoundException;
import com.ubot.web.utils.Config;

import jakarta.ws.rs.ApplicationPath;

//主函式
@ApplicationPath("/api/*")
public class MainApp extends ResourceConfig {

	public MainApp() {
		// 用來掃描該package裡所有@Path並註冊
		packages("com.ubot.web.api");
		register(Config.class);
		register(MissingFileException.class);
		register(NotFoundException.class);
	}

}
