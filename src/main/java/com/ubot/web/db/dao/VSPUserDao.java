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
		String sql = "insert into vspuser(USERID, MANAGER, APPOINTED, SECURITY, DEPT, BRANCH, WORKTYPE) values(?,?,?,?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, user.getUserId());
			ps.setString(2, user.getManager());
			ps.setString(3, user.getAppointed());
			ps.setString(4, user.getSecurity());
			ps.setString(5, user.getDept());
			ps.setString(6, user.getBranch());
			ps.setString(7, user.getWorkType());

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
				String appointed = resultSet.getString("APPOINTED");
				String security = resultSet.getString("SECURITY");
				String dept = resultSet.getString("DEPT");
				String branch = resultSet.getString("BRANCH");
				String workType = resultSet.getString("WORKTYPE");

				user.setUserId(userId);
				user.setBranch(branch);
				user.setManager(manager);
				user.setAppointed(appointed);
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
				String appointed = resultSet.getString("APPOINTED");
				String security = resultSet.getString("SECURITY");
				String dept = resultSet.getString("DEPT");
				String branch = resultSet.getString("BRANCH");
				String workType = resultSet.getString("WORKTYPE");

				user.setUserId(userId);
				user.setBranch(branch);
				user.setManager(manager);
				user.setAppointed(appointed);
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
		String sql = "update vspuser set MANAGER = ?, APPOINTED = ?, SECURITY = ?, DEPT = ?, BRANCH = ?, WORKTYPE = ? where USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, user.getManager());
			ps.setString(2, user.getAppointed());
			ps.setString(3, user.getSecurity());
			ps.setString(4, user.getDept());
			ps.setString(5, user.getBranch());
			ps.setString(6, user.getWorkType());
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
