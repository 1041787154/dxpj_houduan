package com.dxpj.util;

import com.alibaba.fastjson.JSONObject;
import com.dxpj.model.backGround;

public class updateBackGround {
	public JSONObject get_backBround(String ParentID,String PictureTypeID,String AssessTypeID) {
		
		JSONObject jsonObject = new JSONObject(true);
		String sql= "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[backGround] WHERE PictureTypeID = ? AND AssessTypeID = ? AND ParentID = ?";
		backGround pict = backGround.dao.findFirst(sql, PictureTypeID,AssessTypeID,ParentID);
		if (pict!=null) {
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("PictureTypeName", pict.getStr("PictureTypeName"));//图片
			return jsonObject;
		}
		jsonObject.put("status",new JsonMsg(Code.C_112));
		return jsonObject; 
	}

}
