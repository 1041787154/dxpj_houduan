package com.dxpj.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/** * 加密解密工具 * * @author ykc * */
public class EncryptAndDecrypt {
	// 加密长度
	private final static int LENGTH = 128;
	// 编码方式
	private final static String ENCODE = "UTF-8";
	// 秘钥
	private final static String defaultKey = "TRB$A#Yt@v*Bu@W3";
	// 前缀
	//private final static String defaultPrefix = "wangjia:";

	public static void main(String[] args) {
		String mes = "测试数据";
		String e = encrypt(mes);
		System.out.println("加密后：" + e);
		System.out.println("解密后：" + decrypt(e));
	}

	/**
	 * * AES加密后再使用BASE64加密 * 增加前缀 * 1.辨识正常数据，使其不进行解密 * 2.提高安全度 * @param content
	 * * @return * @throws Exception
	 */
	public static String encrypt(String content) {
		String value = "";
		try {
			if (!isEmpty(content)) {
				value = base64Encode(aesEncryptToBytes(content));
			}
		} catch (Exception e) {
			System.out.println("EncryptAndDecrypt（加密错误）");
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * * AES解密后再使用BASE64解密 * 增加前缀 * 1.辨识正常数据，使其不进行解密 * 2.提高安全度 * @param encryptStr
	 * * @return * @throws Exception
	 */
	public static String decrypt(String encryptStr) {
		String value = "";
		try {
			value = aesDecryptByBytes(base64Decode(encryptStr));
		} catch (Exception e) {
			System.out.println("EncryptAndDecrypt（解密错误）");
			e.printStackTrace();
		}
		return value;

	}

	/** * AES加密 * * @param content * @return * @throws Exception */
	public static byte[] aesEncryptToBytes(String content) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(LENGTH, new SecureRandom(defaultKey.getBytes()));
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		SecretKeySpec sks = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, sks);
		return cipher.doFinal(content.getBytes(ENCODE));

	}

	/** * AES解密 * * @param encryptBytes * @return * @throws Exception */
	public static String aesDecryptByBytes(byte[] encryptBytes) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(LENGTH, new SecureRandom(defaultKey.getBytes()));
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		SecretKeySpec sks = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
		cipher.init(Cipher.DECRYPT_MODE, sks);
		byte[] decryptBytes = cipher.doFinal(encryptBytes);
		return new String(decryptBytes);
	}

	/** * BASE64 加密 * * @param content * @return * @throws Exception */
	public static String base64Encode(byte[] bytes) {
		return new BASE64Encoder().encode(bytes);
	}

	/** * BASE64 解密 * * @param content * @return * @throws Exception */
	public static byte[] base64Decode(String base64Code) throws Exception {
		return isEmpty(base64Code) ? null : new BASE64Decoder().decodeBuffer(base64Code);

	}

	public static boolean isEmpty(String str) {
		return null == str || "".equals(str.trim());
	}
}
