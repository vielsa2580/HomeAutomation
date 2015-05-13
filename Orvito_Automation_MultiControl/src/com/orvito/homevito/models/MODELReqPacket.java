package com.orvito.homevito.models;

public class MODELReqPacket {

	
	int sequenceNumber;
	Object actionHandle;
	Long timeOfCreation;
	char msgType;//will be used by TCP receiver during header processing and returning
	Object object;
	char expectedAckId;
	byte[] dataPacketSent;
	
	
	
	public byte[] getDataPacketSent() {
		return dataPacketSent;
	}

	public void setDataPacketSent(byte[] dataPacketSent) {
		this.dataPacketSent = dataPacketSent;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public MODELReqPacket(MODELReqPacket modelReqPacket) {
		this.sequenceNumber = modelReqPacket.getSequenceNumber();
		this.actionHandle = modelReqPacket.getActionHandle();
		this.timeOfCreation=modelReqPacket.getTimeOfCreation();
		this.object=modelReqPacket.getObject();
		this.dataPacketSent = modelReqPacket.getDataPacketSent();
		
	}
	
	public Long getTimeOfCreation() {
		return timeOfCreation;
	}
	public void setTimeOfCreation(Long timeOfCreation) {
		this.timeOfCreation = timeOfCreation;
	}
	public char getMsgType() {
		return msgType;
	}
	public void setMsgType(char msgType) {
		this.msgType = msgType;
	}
	public MODELReqPacket(int sequenceNumber, Object actionHandle,Long timeOfCreation) {
		super();
		this.sequenceNumber = sequenceNumber;
		this.actionHandle = actionHandle;
		this.timeOfCreation=timeOfCreation;
	}
	
	public MODELReqPacket(int sequenceNumber, Object actionHandle,Long timeOfCreation,Object object) {
		super();
		this.sequenceNumber = sequenceNumber;
		this.actionHandle = actionHandle;
		this.timeOfCreation=timeOfCreation;
		this.object=object;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public Object getActionHandle() {
		return actionHandle;
	}
	public void setActionHandle(Object actionHandle) {
		this.actionHandle = actionHandle;
	}
	
	
	
	
}
