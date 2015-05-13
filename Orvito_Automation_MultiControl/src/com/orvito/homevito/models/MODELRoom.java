package com.orvito.homevito.models;

import java.io.Serializable;
import java.util.List;

public class MODELRoom implements Serializable{
	String id,name,userIdFk;
	
	 List<MODELNode> nodeList;

	

	public List<MODELNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<MODELNode> nodeList) {
		this.nodeList = nodeList;
	}

	public MODELRoom(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserIdFk() {
		return userIdFk;
	}

	public void setUserIdFk(String userIdFk) {
		this.userIdFk = userIdFk;
	}
	
	
	

}
