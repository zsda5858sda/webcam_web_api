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

import com.ubot.web.db.vo.WorkReference;

public class WorkReferenceDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public List<WorkReference> selectQuery(String sql) throws Exception {
		List<WorkReference> result = new ArrayList<WorkReference>();
		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		logger.info(sql);
		ResultSet resultSet = stat.executeQuery(sql);
		StringBuilder builder = new StringBuilder();

		while (resultSet.next()) {
			WorkReference workReference = new WorkReference();

			String workName = resultSet.getString("WORKNAME");
			String workType = resultSet.getString("WORKTYPE");

			builder.append("\n");
			builder.append(workName);
			builder.append("  |  ");
			builder.append(workType);

			workReference.setWorkName(workName);
			workReference.setWorkType(workType);

			result.add(workReference);
		}

		System.out.println(builder.toString());
		stat.close();
		resultSet.close();
		conn.close();
		return result;
	}

	public void insertQuery(WorkReference workReference) throws Exception {
		Connection conn = getConnection();
		String sql = "insert into workreference (WORKNAME, WORKTYPE) values (?,?);";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, workReference.getWorkName());
			ps.setString(2, workReference.getWorkType());
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
