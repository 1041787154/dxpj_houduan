package com.dxpj.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.dxpj.util.Code;

import com.dxpj.model.*;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import sun.net.www.content.audio.wav;

import com.alibaba.fastjson.JSONObject;

public class TtsMain {
	//
	// 填写网页上申请的appkey 如 $apiKey="g8eBUMSokVB1BHGmgxxxxxx"
	private static String appKey = "4E1BG9lTnlSeIf1NQFlrSq6h";
	// 填写网页上申请的APP SECRET 如 $secretKey="94dc99566550d87f8fa8ece112xxxxx"
	private static String secretKey = "544ca4657ba8002e3dea3ac2f5fdd241";
	// text 的内容为"欢迎使用百度语音合成"的urlencode,utf-8 编码 // 可以百度搜索"urlencode"
	private static String text = "欢迎使用百度语音";
	// 发音人选择, 0为普通女声，1为普通男生，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女声
	private static int per = 0;
	// 语速，取值0-9，默认为5中语速
	private static int spd = 5;
	// 音调，取值0-9，默认为5中语调
	private static int pit = 5;
	// 音量，取值0-9，默认为5中音量
	private static int vol = 5;
	// private
	public static String url = "http://tsn.baidu.com/text2audio";
	// 可以使用https
	private static String cuid = "1234567JAVA";
	static partyBranch_Wav_MP3 wav_MP3;
	String parentPartyId_static = "029-0001";
//	String assessType = "";

	// private static int ind = 1;

	public static JSONObject tts1(String parentPartyId_static, String assessType, String text) throws Exception {
		wav_MP3 = new partyBranch_Wav_MP3();
		String address;
		JSONObject json = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3] WHERE ParentID = ? AND AssessType = ? AND videoText = ?";
		wav_MP3 = partyBranch_Wav_MP3.dao.findFirst(sql, parentPartyId_static, assessType, text);
		if (wav_MP3 != null) {// 存在text
			if (wav_MP3.getBoolean("isHavingMP3")) {// 已经生成MP3
				json.put("status", new JsonMsg());
				json.put("addressMP3", wav_MP3.get("addressMP3"));
				System.out.println("aaaaa");
				return json;
			} else {// 需要生成MP3
				address = getMP3(assessType, text);
				wav_MP3.set("addressMP3", address);
				wav_MP3.set("isHavingMP3", true);
				wav_MP3.update();
				System.out.println("aaaaa");
				json.put("status", new JsonMsg());
				json.put("addressMP3", address);
				return json;
			}
		} else {
			address = getMP3(assessType, text);
			Record wav_MP3_insert = new Record();
			wav_MP3_insert.set("ParentID", parentPartyId_static);
			wav_MP3_insert.set("AssessType", assessType);
			wav_MP3_insert.set("videoText", text);
			wav_MP3_insert.set("isHavingMP3", true);
			wav_MP3_insert.set("addressMP3", address);
			boolean count = Db.save("[DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3]", wav_MP3_insert);
			if (count) {
				json.put("status", new JsonMsg());
				json.put("addressMP3", address);
				System.out.println("cccccc");
				return json;
			}
		}
		json.put("status", new JsonMsg(Code.C_101));
		return json;

	}

	// public static void main(String[] args) throws Exception {
	//// TtsMain.tts1("029-0001","党性自评端", text);
	// TtsMain.getMP3();
	// }

	public static String getMP3(String assessType,String  text) throws Exception {

//		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3]";
		// List<partyBranch_Wav_MP3> list = partyBranch_Wav_MP3.dao.find(sql);
        String address = null;
//		List<partyBranch_Wav_MP3> list = partyBranch_Wav_MP3.dao.find(sql);
//		String assessType, text;
//		partyBranch_Wav_MP3 wav_MP3;
//		for (int i = 0; i < list.size(); i++) {
//			wav_MP3 = list.get(i);
//			assessType = wav_MP3.getStr("AssessType");
			String temp = null;
			if (assessType.equals("党性自评端")) {
				temp = "zp";
			}
			if (assessType.equals("党性互评端")) {
				temp = "hp";
			}
			if (assessType.equals("党性群众评端")) {
				temp = "qzp";
			}
			if (assessType.equals("党性组织评端")) {
				temp = "zzp";
			}
//			text = wav_MP3.getStr("videoText");
			String fileName = temp + DateUtils.getNowDateTimeStr2();// 命名

			TokenHolder holder = new TokenHolder(appKey, secretKey, TokenHolder.ASR_SCOPE);
			holder.resfresh();
			String token = holder.getToken();
			String url2 = url + "?tex=" + ConnUtil.urlEncode(text);
			url2 += "&per=" + per;
			url2 += "&spd=" + spd;
			url2 += "&pit=" + pit;
			url2 += "&vol=" + vol;
			url2 += "&cuid=" + cuid;
			url2 += "&tok=" + token;
			url2 += "&lan=zh&ctp=1";
			System.out.println(url2);// 反馈请带上此url，浏览器上可以测试
			HttpURLConnection conn = (HttpURLConnection) new URL(url2).openConnection();
			conn.setConnectTimeout(5000);
			String contentType = conn.getContentType();
			if (contentType.contains("mp3")) {
				byte[] bytes = ConnUtil.getResponseBytes(conn);

				String saveDir = "E:/nodeProgram/dxpj/program/" + temp + "/static/assets/";
				
				File file = new File(saveDir);
				if (!file.exists()) {
					file.mkdirs();
				}
				System.out.println("mp3 file writes to " + saveDir + fileName + ".mp3");
				System.out.println("mp3 file : " + file.getAbsolutePath());
				//
				File file1 = new File(saveDir + fileName + ".mp3");
				System.out.println("mp3 file1 : " + file1.getAbsolutePath());
				// 打开mp3文件即可播放 //
				FileOutputStream os = new FileOutputStream(file1);
				os.write(bytes);
				os.close();
				address = "static/assets/"+fileName+".mp3";
				System.out.println("mp3 file writes to " + saveDir + fileName + ".mp3");
				
			} else {
				System.err.println("ERROR: content-type= " + contentType);
				String res = ConnUtil.getResponseString(conn);
				System.err.println(res);
			}

//		}
		return address;
	}
}
