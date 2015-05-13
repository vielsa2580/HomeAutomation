package com.orvito.homevito.models;

public class MODELLiveStream {

	
	public MODELLiveStream(String name, String url) {
		super();
		this.url = url;
		this.name = name;
	}

	String url,name;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
