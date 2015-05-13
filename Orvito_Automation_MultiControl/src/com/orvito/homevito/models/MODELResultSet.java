package com.orvito.homevito.models;

import java.util.List;

import org.json.JSONObject;

public final  class MODELResultSet {
	
	String error,message,sessionId;	
	List<?> dataList;	
	JSONObject jsonObject;
	Object singleDataObject;
	String msgType,sequenceNumber;
	
	

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Object getSingleDataObject() {
		return singleDataObject;
	}

	public void setSingleDataObject(Object singleDataObject) {
		this.singleDataObject = singleDataObject;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public MODELResultSet(String error) {
		super();
		this.error = error;
	}

	public MODELResultSet(JSONObject jsonObject) {
		super();
		this.jsonObject = jsonObject;
	}
	
	

	public MODELResultSet(String error, String message) {
		super();
		this.error = error;
		this.message = message;
	}

	public MODELResultSet() {
		super();
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<?> getDataList() {
		return dataList;
	}

	public void setDataList(List<?> dataList) {
		this.dataList = dataList;
	}
	
	
	

}
