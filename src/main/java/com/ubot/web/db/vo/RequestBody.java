package com.ubot.web.db.vo;

import jakarta.ws.rs.QueryParam;

//僅接收來自前端帶過來的URL參數，與DB無實際關聯
public class RequestBody {
	@QueryParam("minDate")
	private String minDate;

	@QueryParam("maxDate")
	private String maxDate;

	@QueryParam("userId")
	private String userId;

	@QueryParam("workType")
	private String workType;

	@QueryParam("branch")
	private String branch;

	public String getMinDate() {
		return minDate;
	}

	public String getMaxDate() {
		return maxDate;
	}

	public String getUserId() {
		return userId;
	}

	public String getWorkType() {
		return workType;
	}

	public String getBranch() {
		return branch;
	}

	@Override
	public String toString() {
		String result = "";
		if (minDate != null && !minDate.equals("")) {
			result += "minDate=" + minDate;
		}
		if (maxDate != null && !maxDate.equals("")) {
			result += ", maxDate=" + maxDate;
		}
		if (userId != null && !userId.equals("")) {
			result += ", userId=" + userId;
		}
		if (workType != null && !workType.equals("")) {
			result += ", workType=" + workType;
		}
		if (branch != null && !branch.equals("")) {
			result += ", branch=" + branch;
		}
		return result;
	}
}
