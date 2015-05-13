package com.orvito.homevito.models;

import java.util.List;

public class MODELBlock {
	
	public MODELBlock(String blockName, List<MODELUser> modelResidentList) {
		super();
		this.blockName = blockName;
		this.modelUserList = modelResidentList;
	}
	public MODELBlock() {
		// TODO Auto-generated constructor stub
	}
	String id,blockName;
	List<MODELUser> modelUserList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBlockName() {
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	public List<MODELUser> getModelResidentList() {
		return modelUserList;
	}
	public void setModelUserList(List<MODELUser> modelResidentList) {
		this.modelUserList = modelResidentList;
	}
	
}
