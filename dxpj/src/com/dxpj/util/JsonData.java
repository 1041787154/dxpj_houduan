package com.dxpj.util;

public class JsonData extends JsonMsg{
	
	private Object data;

	public Object getData() {
		return data;
	}

	public JsonData appendData(Object data) {
		this.data = data;
		return this;
	}
	
	
}
