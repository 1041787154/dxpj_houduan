package com.dxpj.util;

public class JsonMsg {
	private int status = 200;
	private Code code = Code.C_100;
	private String msg = null;
	
	public JsonMsg(){}
	
	public JsonMsg(String msg){
		this.msg = msg;
	}
	
	public JsonMsg(Code code){
		this.code = code;
	}
	
	public JsonMsg(Code code, String msg){
		this.code = code;
		this.msg = msg;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCode() {
		return code.getCode();
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public String getMsg() {
		if(msg == null){
			return code.getMsg();
		}
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
