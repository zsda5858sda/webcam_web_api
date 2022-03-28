package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.UserToken;

//有關對usertoken表的CRUD
public class UserTokenDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public Optional<UserToken> selectById(String id) throws Exception {
		Connection conn = getConnection();
		String sql = String.format("select * from usertoken where SESSIONID = '%s'", id);
		logger.info(sql);
		UserToken userToken = new UserToken();
		try (Statement stat = conn.createStatement(); ResultSet resultSet = stat.executeQuery(sql);) {

			while (resultSet.next()) {
				userToken = new UserToken();
				String tokenId = resultSet.getString("tokenId");
				String userId = resultSet.getString("userId");

				userToken.setTokenId(tokenId);
				userToken.setUserId(userId);
			}
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}

		return Optional.ofNullable(userToken);
	}

	public void deleteBeforeInsertQuery(UserToken userToken) throws Exception {
		Connection conn = getConnection();
		String deleteSql = "delete from usertoken where USERID = ?";
		String insertSql = "insert into usertoken (TOKENID, USERID, IP) values (?,?,?);";
		try (PreparedStatement dps = conn.prepareStatement(deleteSql);
				PreparedStatement ips = conn.prepareStatement(insertSql)) {

			dps.setString(1, userToken.getUserId());
			logger.info(dps.toString());
			dps.execute();
			dps.close();

			ips.setString(1, userToken.getTokenId());
			ips.setString(2, userToken.getUserId());
			ips.setString(3, userToken.getIp());
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
