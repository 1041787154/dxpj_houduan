package com.dxpj.util;

import java.util.List;

import com.dxpj.model.backGround;
import com.dxpj.model.partyBranch_Wav_MP3;

public class changeSqlserver {
	public static JsonMsg decImg() {
		
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[backGround] WHERE ParentID = ?";
		List<backGround> backs = backGround.dao.find(sql, "029-0001");
		
		for (backGround back : backs) {
			String pictName = back.get("PictureTypeName");
			
			back.set("PictureTypeName", pictName.substring(1));
			back.update();
		}
		return null;
		
		
	}
	public static JsonMsg addImg() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[backGround] WHERE ParentID = ?";
		List<backGround> backs = backGround.dao.find(sql, "029-0001");
		
		for (backGround back : backs) {
			String pictName = back.get("PictureTypeName");
			
			back.set("PictureTypeName","../../"+pictName);
			back.update();
		}
		return null;
		
		
	}
	public static JsonMsg decMp3() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3] WHERE ParentID = ? AND isHavingMP3='1'";
		List<partyBranch_Wav_MP3> backs = partyBranch_Wav_MP3.dao.find(sql, "029-0001");
		
		for (partyBranch_Wav_MP3 back : backs) {
			String pictName = back.get("addressMP3");
			
			back.set("addressMP3", pictName.substring(6));
			back.update();
		}
		return null;
		
		
	}
	public static JsonMsg addMp3() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3] WHERE ParentID = ?";
		List<partyBranch_Wav_MP3> backs = partyBranch_Wav_MP3.dao.find(sql, "029-0001");
		
		for (partyBranch_Wav_MP3 back : backs) {
			String pictName = back.get("addressMP3");
			
			back.set("addressMP3","../../"+pictName);
			back.update();
		}
		return null;
		
		
	}
	public static JsonMsg setMp3False() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3] WHERE ParentID = ?";
		List<partyBranch_Wav_MP3> backs = partyBranch_Wav_MP3.dao.find(sql, "029-0001");
		
		for (partyBranch_Wav_MP3 back : backs) {
			
			back.set("addressMP3",null);
			back.set("isHavingMP3",false);
			back.update();
		}
		return null;
		
		
	}

}
