package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
				vspFile.setWorkType(resultSet.getString("WORKTYPE"));
				vspFile.setBranch(resultSet.getString("BRANCH"));
				vspFile.setMemo(resultSet.getString("MEMO"));

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				String datetime = LocalDateTime.parse(resultSet.getString("WORKDATE"), formatter)
						.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				vspFile.setWorkDate(datetime);

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

	public void insertQuery(VSPFile file) throws Exception {
		Connection conn = getConnection();
		String sql = "insert into vspfile(FILENAME, FILEPATH, WORKTYPE, BRANCH, WORKDATE, MEMO) values(?,?,?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql);) {

			ps.setString(1, file.getFileName());
			ps.setString(2, file.getFilePath());
			ps.setString(3, file.getWorkType());
			ps.setString(4, file.getBranch());
			ps.setString(5, file.getWorkDate());
			ps.setString(6, file.getMemo());

			logger.info(ps.toString());
			ps.execute();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
}
