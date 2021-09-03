package com.ubot.web.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ubot.web.db.vo.VSPValidate;

// 有關對vspvalidate表的CRUD
public class VSPValidateDao extends BaseDao {

	public VSPValidate selectQuery(String sql) throws Exception {
		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		ResultSet resultSet = stat.executeQuery(sql);
		VSPValidate result = new VSPValidate();

		while (resultSet.next()) {
			String validate = resultSet.getString("VALIDATE");

			result.setValidate(validate);
		}
		stat.close();
		resultSet.close();
		conn.close();
		return result;
	}

	public void updateQuery(VSPValidate validate) throws Exception {
		Connection conn = getConnection();
		String sql = "update vspvalidate set VALIDATE = ?";
		PreparedStatement ps = conn.prepareStatement(sql);

		ps.setString(1, validate.getValidate());

		ps.execute();
		ps.close();
		conn.close();
	}
}
