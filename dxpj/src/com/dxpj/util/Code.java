package com.dxpj.util;

public enum Code {
	C_100(100, "操作成功"),
	C_101(101, "操作失败"),
	C_102(102, "尚无激活的活动"),
	C_103(103, "尚没有资格参加评价的党支部"),
	C_104(104, "该支部没有进行党性评价的成员"),
	C_105(105, "您已经完成提交，无需再次登录"),
	C_106(106, "验证失败"),
	C_107(107, "保存答案失败"),	
	C_108(108, "无参评对象"),
	C_109(109, "未评价完80%的组员"),
	C_110(110, "无上级党组织"),
	C_111(111, "上级党组织id有误"),
	C_112(112, "背景图上传失败");
	
	private int code;
	private String msg;
	
	private Code(int code, String msg){
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
