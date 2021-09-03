package com.ubot.web.db.vo;

//後臺使用者資料，對應vspuser表
public class VSPUser {
	private String userId;
	private String manager;
	private String security;
	private String dept;
	private String branch;
	private String workType;
	private String subordinate;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public String getSubordinate() {
		return subordinate;
	}

	public void setSubordinate(String subordinate) {
		this.subordinate = subordinate;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}
}
