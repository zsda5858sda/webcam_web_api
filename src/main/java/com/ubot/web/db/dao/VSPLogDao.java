package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.Log;
import com.ubot.web.db.vo.VSPLog;

// 有關對所有log表的CRUD
public class VSPLogDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public void insertQuery(VSPLog log) throws Exception {
		Connection conn = getConnection();
		String sql = "insert into vsplog(USERID, CREATEDATETIME, ACTION, IP) values(?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql);) {

			ps.setString(1, log.getUserId());
			ps.setString(2, log.getCreateDatetime());
			ps.setString(3, log.getAction());
			ps.setString(4, log.getIp());

			logger.info(ps.toString());
			ps.execute();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	// 查詢後台log的資料庫操作
	public List<VSPLog> searchVSPLog(String sql) throws Exception {
		Connection conn = getConnection();
		List<VSPLog> result = new ArrayList<VSPLog>();
		StringBuilder builder = new StringBuilder();
		logger.info(sql);
		try (Statement stat = conn.createStatement(); ResultSet resultSet = stat.executeQuery(sql);) {
			while (resultSet.next()) {
				VSPLog log = new VSPLog();

				String userId = resultSet.getString("USERID");
				String createDatetime = resultSet.getString("CREATEDATETIME");
				String action = resultSet.getString("ACTION");
				String ip = resultSet.getString("IP");

				builder.append("\n");
				builder.append(userId);
				builder.append("  |  ");
				builder.append(createDatetime);
				builder.append("  |  ");
				builder.append(action);
				builder.append("  |  ");
				builder.append(ip);

				log.setAction(action);
				log.setCreateDatetime(createDatetime);
				log.setIp(ip);
				log.setUserId(userId);

				result.add(log);
			}
			logger.info(builder.toString());
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
		return result;
	}

	// 查詢app log的資料庫操作
	public List<Log> searchAppLog(String sql) throws Exception {
		Connection conn = getConnection();
		List<Log> result = new ArrayList<Log>();
		StringBuilder builder = new StringBuilder();
		logger.info(sql);
		try (Statement stat = conn.createStatement(); ResultSet resultSet = stat.executeQuery(sql);) {
			while (resultSet.next()) {
				Log log = new Log();

				String userId = resultSet.getString("USERID");
				String userType = resultSet.getString("USERTYPE");
				String createDatetime = resultSet.getString("CREATEDATETIME");
				String action = resultSet.getString("ACTION");
				String ip = resultSet.getString("IP");

				builder.append("\n");
				builder.append(userId);
				builder.append("  |  ");
				builder.append(userType);
				builder.append("  |  ");
				builder.append(createDatetime);
				builder.append("  |  ");
				builder.append(action);
				builder.append("  |  ");
				builder.append(ip);

				log.setAction(action);
				log.setUserType(userType);
				log.setCreateDatetime(createDatetime);
				log.setIp(ip);
				log.setUserId(userId);

				result.add(log);
			}
			logger.info(builder.toString());
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
		return result;
	}

}
