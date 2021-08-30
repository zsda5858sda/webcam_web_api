package com.ubot.web.db.vo;

import jakarta.ws.rs.QueryParam;

public class RequestBody {
	@QueryParam("minDate")
	private String minDate;

	@QueryParam("maxDate")
	private String maxDate;

	@QueryParam("userId")
	private String userId;

	@QueryParam("workType")
	private String workType;

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

	@Override
	public String toString() {
		String result = "";
		if (minDate != null) {
			result += "minDate=" + minDate;
		}
		if (maxDate != null) {
			result += ", maxDate=" + maxDate;
		}
		if (userId != null) {
			result += ", userId=" + userId;
		}
		if (workType != null) {
			result += ", workType=" + workType;
		}
		return result;
	}
}
