package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.VSPBranch;

public class VSPBranchDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public List<VSPBranch> selectQuery(String sql) throws Exception {
		List<VSPBranch> result = new ArrayList<VSPBranch>();
		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		logger.info(sql);
		ResultSet resultSet = stat.executeQuery(sql);
		StringBuilder builder = new StringBuilder();

		while (resultSet.next()) {
			VSPBranch branch = new VSPBranch();

			String branchCode = resultSet.getString("BRANCHCODE");
			String branchName = resultSet.getString("BRANCHNAME");

			builder.append("\n");
			builder.append(branchCode);
			builder.append("  |  ");
			builder.append(branchName);

			branch.setBranchCode(branchCode);
			branch.setBranchName(branchName);
			result.add(branch);
		}

		logger.info(builder.toString());
		stat.close();
		resultSet.close();
		conn.close();
		return result;
	}
}
