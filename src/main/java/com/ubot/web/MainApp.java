package com.ubot.web;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.ubot.web.config.Config;
import com.ubot.web.config.XSSFilter;
import com.ubot.web.exception.NotFoundException;
import com.ubot.web.exception.PrimaryKeyException;
import com.ubot.web.exception.UnknownException;

import jakarta.ws.rs.ApplicationPath;

//主函式
@ApplicationPath("/api/*")
public class MainApp extends ResourceConfig {

	public MainApp() {
		// 用來掃描該package裡所有@Path並註冊
		packages("com.ubot.web.api");
		register(Config.class);
		register(XSSFilter.class);
		register(NotFoundException.class);
		register(UnknownException.class);
		register(PrimaryKeyException.class);
		register(MultiPartFeature.class);
	}

}
