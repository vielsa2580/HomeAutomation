package com.orvito.homevito.models;

import java.util.List;

public class MODELNode {


	String id,name,devOwnerIdFk,nodeNum,ipAddress,port,devAuthToken,state;
	MODELNodeStatus nodeStatus;
	MODELHardwareType hardwareType;
	int uiObjectID;
	
	public MODELNode(String name, String ipAddress, String port,String devAuthToken,String nodeNum,MODELHardwareType hardwareType) {
		super();
		this.name = name;
		this.ipAddress = ipAddress;
		this.port = port;
		this.devAuthToken=devAuthToken;
		this.nodeNum=nodeNum;
		this.hardwareType = hardwareType;
	}
	
	
	public MODELNode() {
		// TODO Auto-generated constructor stub
	}


	public MODELNode(MODELNode modelNode) {
		super();
		this.name = modelNode.getName();
		this.ipAddress = modelNode.getIpAddress();
		this.port = modelNode.getPort();
		this.devAuthToken=modelNode.getDevAuthToken();
		this.nodeNum=modelNode.getNodeNum();
		this.hardwareType = modelNode.getHardwareType();
		this.nodeStatus=modelNode.getNodeStatus();
	}


	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDevAuthToken() {
		return devAuthToken;
	}

	public void setDevAuthToken(String devAuthToken) {
		this.devAuthToken = devAuthToken;
	}

	
	
	
	
	
	
	
	public int getUiObjectID() {
		return uiObjectID;
	}

	public void setUiObjectID(int uiObjectID) {
		this.uiObjectID = uiObjectID;
	}

	

	public MODELHardwareType getHardwareType() {
		return hardwareType;
	}

	public void setHardwareType(MODELHardwareType hardwareType) {
		this.hardwareType = hardwareType;
	}

	public MODELNodeStatus getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(MODELNodeStatus nodeStatus) {
		this.nodeStatus = nodeStatus;
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

	public String getDevOwnerIdFk() {
		return devOwnerIdFk;
	}

	public void setDevOwnerIdFk(String devOwnerIdFk) {
		this.devOwnerIdFk = devOwnerIdFk;
	}

	public String getNodeNum() {
		return nodeNum;
	}

	public void setNodeNum(String nodeNum) {
		this.nodeNum = nodeNum;
	}

	

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}


}
