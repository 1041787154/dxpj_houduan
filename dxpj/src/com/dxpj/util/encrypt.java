package com.dxpj.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class encrypt {
	
	private static String sKey = "TRB$A#Yt@v*Bu@W3";
	
	/**
	 * 加密
	 *
	 * @param sSrc 加密的明文
	 * @param sKey 秘钥
	 * @return
	 * @throws Exception
	 */
	
	public static String Encrypt(String sSrc) throws Exception {
		if (sKey == null) {
			System.out.print("Key不能为空null");
			return null;
		}
		SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes());
		String base64Result = Base64.getEncoder().encodeToString(encrypted);// 对加密后的字节数组进行Base64编码
		return base64Result;
//	 new BASE64Encoder().encode(encrypted);
	}

	/**
	 * 解密
	 * 
	 * @param sSrc 接收到的加密过后的字符串（带解密密文）
	 * @param sKey 秘钥
	 * @return
	 * @throws Exception
	 */
	public static String Decrypt(String sSrc) throws Exception {
		try {
			if (sKey == null) {
				System.out.print("Key不能为空null");
				return null;
			}
//	        byte[] byte1 = Base64.decode(sSrc);//先用Base64解码
			byte[] byte1 = Base64.getDecoder().decode(sSrc.getBytes());
			SecretKeySpec key = new SecretKeySpec(sKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			// 与加密时不同MODE:Cipher.DECRYPT_MODE
			byte[] ret = cipher.doFinal(byte1);
			return new String(ret, "utf-8");
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}


}
