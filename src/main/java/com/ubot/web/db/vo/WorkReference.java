package com.ubot.web.db.vo;

//業務種類代碼，對應workreference表
public class WorkReference {
	private String workName;
	private String workType;

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}
}
