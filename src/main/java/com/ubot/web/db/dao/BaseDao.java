package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class BaseDao {
	private static String driverName = "com.mysql.cj.jdbc.Driver";
	private static String dbURL = "jdbc:mysql://192.168.198.130:3306/ubot";
	private static String userName = "centos";
	private static String password = "password";
	// server db
//	private static String dbURL = "jdbc:mysql://172.16.45.245:3306/vsp";
//	private static String userName = "apuser";
//	private static String password = "apuser";

	public Connection getConnection() throws Exception {
		Connection conn = null;
		Class.forName(driverName);
		conn = DriverManager.getConnection(dbURL, userName, password);
		return conn;
	}
}
