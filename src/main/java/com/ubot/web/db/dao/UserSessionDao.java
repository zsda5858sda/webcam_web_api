package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.UserSession;

//有關對usersession表的CRUD
public class UserSessionDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public Optional<UserSession> selectById(String id) throws Exception {
		Connection conn = getConnection();
		String sql = String.format("select * from usersession where SESSIONID = '%s'", id);
		logger.info(sql);
		UserSession userSession = null;
		try (Statement stat = conn.createStatement(); ResultSet resultSet = stat.executeQuery(sql);) {

			while (resultSet.next()) {
				userSession = new UserSession();
				String sessionId = resultSet.getString("sessionId");
				String userId = resultSet.getString("userId");

				userSession.setSessionId(sessionId);
				userSession.setUserId(userId);
			}
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}

		return Optional.ofNullable(userSession);
	}

	public void deleteBeforeInsertQuery(UserSession userSession) throws Exception {
		Connection conn = getConnection();
		String deleteSql = "delete from usersession where USERID = ?";
		String insertSql = "insert into usersession (SESSIONID, USERID, IP) values (?,?,?);";
		try (PreparedStatement dps = conn.prepareStatement(deleteSql);
				PreparedStatement ips = conn.prepareStatement(insertSql)) {

			dps.setString(1, userSession.getUserId());
			logger.info(dps.toString());
			dps.execute();
			dps.close();

			ips.setString(1, userSession.getSessionId());
			ips.setString(2, userSession.getUserId());
			ips.setString(3, userSession.getIp());
			logger.info(ips.toString());
			ips.execute();
			ips.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
}
