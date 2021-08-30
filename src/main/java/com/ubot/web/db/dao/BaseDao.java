package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

public class BaseDao {
	private static String driverName;
	private static String dbURL;
	private static String userName;
	private static String poss_word;

	static {
		ResourceBundle rs = ResourceBundle.getBundle("DB");
		driverName = rs.getString("DRIVER").trim();
		dbURL = rs.getString("URL").trim();
		userName = rs.getString("USER").trim();
		poss_word = rs.getString("PASSWORD").trim();
	}

	public Connection getConnection() throws Exception {
		Connection conn = null;
		Class.forName(driverName);
		conn = DriverManager.getConnection(dbURL, userName, poss_word);
		return conn;
	}
}
