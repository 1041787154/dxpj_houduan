package com.dxpj.util;

import com.alibaba.fastjson.JSONObject;
import com.dxpj.model.PartyBranch_Wav;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import jdk.internal.dynalink.beans.StaticClass;

public class Video {

	private static String Volume;
	private static String Rate;
	
	/**
	 * <p>
	 * 自定义播音提示<br>
	 * 自己输入简短的文字， 进行播音提示
	 * 
	 * @param words
	 *            自定义提示文字
	 * @return jsonMsg 返回的状态
	 */
	private static ActiveXComponent sap;
	private static Dispatch sapo;
	
	public JsonMsg video_words(JSONObject s) {
		String words = s.getString("words");
		JsonMsg jsonMsg = new JsonMsg();
		sap = new ActiveXComponent("Sapi.SpVoice");
		if (Volume ==null) {
			String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_Wav] WHERE IsValid = '1'";
			PartyBranch_Wav partyBranch_Wav = PartyBranch_Wav.dao.findFirst(sql);
			Volume = partyBranch_Wav.getStr("Volume");
			System.out.println("Volume"+Volume);
		}
		if (Rate ==null) {
			String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_Wav] WHERE IsValid = '1'";
			PartyBranch_Wav partyBranch_Wav = PartyBranch_Wav.dao.findFirst(sql);
			Rate = partyBranch_Wav.getStr("Rate");
			System.out.println("Rate"+Rate);
		}
		try {
			
			// 音量 0-100
			//100
			sap.setProperty("Volume", new Variant(Integer.parseInt(Volume)));
			// 语音朗读速度 -10 到 +10
			//-2
			sap.setProperty("Rate", new Variant(Integer.parseInt(Rate)));
			// 获取执行对象
			sapo = sap.getObject();
			// 执行朗读
			System.out.println("words:"+words);
			Dispatch.call(sapo, "Speak", new Variant(words));
			// 关闭执行对象
			sapo.safeRelease();
			jsonMsg = new JsonMsg();
		} catch (Exception e) {
			jsonMsg = new JsonMsg(Code.C_101);
			e.printStackTrace();
		} finally {
			// 关闭应用程序连接
			sap.safeRelease();
		}
		return jsonMsg;
	}
	
	//关闭播音
	public JsonMsg close_video(){
		JsonMsg jsonMsg = new JsonMsg();
		try{
			sapo.safeRelease();
			sapo.safeRelease();
		}catch (Exception e) {
			// TODO: handle exception
			jsonMsg = new JsonMsg(Code.C_101);
		}
		
		return jsonMsg;
		
	}
}
