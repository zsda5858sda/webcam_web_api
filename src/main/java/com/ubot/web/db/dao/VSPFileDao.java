package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ubot.web.db.vo.VSPFile;

// 有關對vspfile表的CRUD
public class VSPFileDao extends BaseDao {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public List<VSPFile> selectQuery(String sql) throws Exception {
		sql += " order by SUBSTR(FILENAME, 1, 7) ASC, WORKDATE DESC";
		List<VSPFile> result = new ArrayList<VSPFile>();
		Connection conn = getConnection();
		try {
			Statement stat = conn.createStatement();
			logger.info(sql);
			ResultSet resultSet = stat.executeQuery(sql);

			while (resultSet.next()) {
				VSPFile vspFile = new VSPFile();

				vspFile.setVid(resultSet.getInt("VID"));
				vspFile.setFileName(resultSet.getString("FILENAME"));
				vspFile.setFilePath(resultSet.getString("FILEPATH"));
				vspFile.setWorkDate(resultSet.getString("WORKDATE"));
				vspFile.setWorkType(resultSet.getString("WORKTYPE"));
				vspFile.setBranch(resultSet.getString("BRANCH"));

				result.add(vspFile);
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
}
