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

	@QueryParam("customerId")
	private String customerId;

	@QueryParam("date")
	private String date;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

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
		if (customerId != null && !customerId.equals("")) {
			result += ", customerId=" + customerId;
		}
		if (date != null && !date.equals("")) {
			result += ", date=" + date;
		}
		if (result.indexOf(",") == 0) {
			result = result.substring(2);
		}
		return result;
	}
}
