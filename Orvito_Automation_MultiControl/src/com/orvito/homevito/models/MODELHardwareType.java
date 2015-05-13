package com.orvito.homevito.models;

public class MODELHardwareType {
	
	String id,name;
	
	
	

	public MODELHardwareType(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public MODELHardwareType(String name) {
		super();
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

}
