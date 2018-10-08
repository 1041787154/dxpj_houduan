package com.dxpj.server;

import java.util.List;

import com.dxpj.model.user_pass;
import com.dxpj.util.Code;
import com.dxpj.util.GetNumOfcomputer;
import com.dxpj.util.JsonMsg;

public class comServer {
	private static String pass;
	public static JsonMsg judge_IsAssess() {
		pass = GetNumOfcomputer.getNumOfcomputer1();
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[user_pass] WHERE isValid = '1'";
		List<user_pass> uPass = user_pass.dao.find(sql);// 获得已经激活的活动集合
		if(uPass!=null&&uPass.size()!=0) {
			
			for (user_pass user_pass : uPass) {
				if(user_pass.get("password").equals(pass)) {
					return new JsonMsg();
				}
			}
		}
		return new JsonMsg(Code.C_101);
	}
	
	public static JsonMsg register_(String user, String phone) {
		user_pass user_ = new user_pass();
		user_.set("userName", user);
		user_.set("phone", phone);
		user_.set("password", pass);
		user_.set("isValid", 1);
		boolean b = user_.save();
		if(b) {
			return new JsonMsg();
		}
		return new JsonMsg(Code.C_101);
	}
}
