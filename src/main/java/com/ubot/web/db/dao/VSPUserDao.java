package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.VSPUser;

// 有關對vspuser表的CRUD
public class VSPUserDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public void insertQuery(VSPUser user) throws Exception {
		Connection conn = getConnection();
		String sql = "insert into vspuser(USERID, MANAGER, SECURITY, DEPT, BRANCH, WORKTYPE, SUBORDINATE) values(?,?,?,?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, user.getUserId());
			ps.setString(2, user.getManager());
			ps.setString(3, user.getSecurity());
			ps.setString(4, user.getDept());
			ps.setString(5, user.getBranch());
			ps.setString(6, user.getWorkType());
			ps.setString(7, user.getSubordinate());

			logger.info(ps.toString());
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	public List<VSPUser> selectQuery(String sql) throws Exception {
		List<VSPUser> result = new ArrayList<VSPUser>();
		Connection conn = getConnection();
		try {
			Statement stat = conn.createStatement();
			logger.info(sql);
			ResultSet resultSet = stat.executeQuery(sql);

			while (resultSet.next()) {
				VSPUser user = new VSPUser();

				String userId = resultSet.getString("USERID");
				String manager = resultSet.getString("MANAGER");
				String security = resultSet.getString("SECURITY");
				String dept = resultSet.getString("DEPT");
				String branch = resultSet.getString("BRANCH");
				String workType = resultSet.getString("WORKTYPE");
				String subordinate = resultSet.getString("SUBORDINATE");

				user.setUserId(userId);
				user.setBranch(branch);
				user.setManager(manager);
				user.setSubordinate(subordinate);
				user.setWorkType(workType);
				user.setDept(dept);
				user.setSecurity(security);

				result.add(user);
			}

			stat.close();
			resultSet.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
		return result;
	}

	public void updateSubordinate(List<VSPUser> userList) throws Exception {
		Connection conn = getConnection();

		StringBuilder builder = new StringBuilder();

		userList.forEach(u -> {

			String sql = "update `vspuser` set SUBORDINATE = ? where USERID = ?;";

			try (PreparedStatement ps = conn.prepareStatement(sql)) {

				ps.setString(1, u.getSubordinate());
				ps.setString(2, u.getUserId());

				builder.append(ps.toString().split(": ")[1]);

				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		});

		logger.info(builder.toString());
		conn.close();
	}

	public Optional<VSPUser> findById(String id) throws Exception {
		Connection conn = getConnection();
		VSPUser user = new VSPUser();
		try {
			String sql = "select * from vspuser where USERID = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, id);

			logger.info(ps.toString());
			ResultSet resultSet = ps.executeQuery();

			while (resultSet.next()) {
				String userId = resultSet.getString("USERID");
				String manager = resultSet.getString("MANAGER");
				String security = resultSet.getString("SECURITY");
				String dept = resultSet.getString("DEPT");
				String branch = resultSet.getString("BRANCH");
				String workType = resultSet.getString("WORKTYPE");
				String subordinate = resultSet.getString("SUBORDINATE");

				user.setUserId(userId);
				user.setBranch(branch);
				user.setManager(manager);
				user.setSubordinate(subordinate);
				user.setWorkType(workType);
				user.setDept(dept);
				user.setSecurity(security);
			}

			ps.close();
			resultSet.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
		return user.getUserId() == null ? Optional.empty() : Optional.of(user);
	}

	public void updateQuery(VSPUser user) throws Exception {
		Connection conn = getConnection();
		String sql = "update vspuser set MANAGER = ?, SECURITY = ?, DEPT = ?, BRANCH = ?, WORKTYPE = ?, SUBORDINATE = ? where USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, user.getManager());
			ps.setString(2, user.getSecurity());
			ps.setString(3, user.getDept());
			ps.setString(4, user.getBranch());
			ps.setString(5, user.getWorkType());
			ps.setString(6, user.getSubordinate());
			ps.setString(7, user.getUserId());

			logger.info(ps.toString());
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

}
