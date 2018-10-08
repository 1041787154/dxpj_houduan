package com.dxpj.util;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.dxpj.util.encrypt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

public class GetNumOfcomputer {
	private static String saveDir = "C:/dxpj/";

	public static boolean newFile(File file1) {
		boolean bool = false;
		if (file1.exists()) {
			System.out.println("file exists");
			bool = true;
		} else {
			System.out.println("file not exists, create it ...");
			bool = false;
			try {
				file1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bool;
	}

	public static String getNumOfcomputer1() {

		String Txt = "";
		String encrypt1 = "";
		String decrypt = "";
		File file = new File(saveDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		File file1 = new File(saveDir + "shebeihao" + ".txt");
		if (newFile(file1)) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file1), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String lineTxt = "";
				while ((lineTxt = br.readLine()) != null) {
					Txt += lineTxt;
				}
				System.out.println("------Txt：（从文件获取）--------" + Txt);
				br.close();
			} catch (Exception e) {
				System.out.println("文件读取错误!");
			}
			System.out.println("主板:  SN:"+ getMotherboardSN());
			System.out.println("C盘:  SN:"+getHardDiskSN("c"));
		} else {
			System.out.println("----------需要现在生成验证码-----------");
			/* Txt = pinjie(getSerialNumber("C:/"), getMac()); */
			/* Txt = pinjie(getCPUSerial(), getMac()); */
//		    Txt = getMac(); 

			Txt = "C4-34-6B-01-4D-3B";
			try {
				encrypt1 = encrypt.Encrypt(Txt);
				decrypt = encrypt.Decrypt(encrypt1);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("----------设备号：（现查--加密前）-----------" + Txt);// 硬盘编号+mac
			System.out.println("----------设备号：（现查--加密后）-----------" + encrypt1);// 硬盘编号+mac
			System.out.println("----------设备号：（现查--解密后）-----------" + decrypt);
			System.out.println("主板:  SN:"+ getMotherboardSN());
			System.out.println("C盘:  SN:"+getHardDiskSN("c"));
			try {
				FileOutputStream fos = new FileOutputStream(file1);
				fos.write(encrypt1.getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return encrypt1;
	}

	public static JsonMsg getNumOfcomputer(String num) {
		JsonMsg jsonMsg = new JsonMsg();
		String Txt = getNumOfcomputer1();
		if (!Txt.equals(num)) {
			jsonMsg = new JsonMsg(Code.C_106);
		}
		return jsonMsg;

	}

	public static String getMac() {
		String line, MACAddr = "";
		;

		// TODO Auto-generated method stub
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append("-");
				}
				// 字节转换为整数
				int temp = mac[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			MACAddr = sb.toString().toUpperCase();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取网卡，获取地址
		catch (SocketException e) {
			e.printStackTrace();
		}

		System.out.println("-------本机MAC地址-------------:" + MACAddr);
		MACAddr = MACAddr.replaceAll("-", "");
		return MACAddr;
	}

	/**
	 * 获取主板序列号
	 *
	 * @return
	 */
	public static String getMotherboardSN() {
		String result = "";
		try {
			File file = File.createTempFile("realhowto", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);
			String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
					+ "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_BaseBoard\") \n"
					+ "For Each objItem in colItems \n" + "    Wscript.Echo objItem.SerialNumber \n"
					+ "    exit for  ' do the first cpu only! \n" + "Next \n";
			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.trim();

	}
	/**
     * 获取硬盘序列号
     *
     * @param drive
     *            盘符
     * @return
     */
	public static String getHardDiskSN(String drive) {
		String result = "";
		try {
			File file = File.createTempFile("damn", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);

			String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
					+ "Set colDrives = objFSO.Drives\n" + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
					+ "Wscript.Echo objDrive.SerialNumber"; // see note
			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;

			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("--------硬盘编号---------:" + result.trim());
		return result.trim();
	}

	/**
     * 获取CPU序列号
     *
     * @return
     */
	public static String getCPUSerial() {
		String result = "";
		try {
			File file = File.createTempFile("tmp_01", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);
			String vbs = "On Error Resume Next \r\n\r\n" + "strComputer = \".\"  \r\n"
					+ "Set objWMIService = GetObject(\"winmgmts:\" _ \r\n"
					+ "    & \"{impersonationLevel=impersonate}!\\\\\" & strComputer & \"\\root\\cimv2\") \r\n"
					+ "Set colItems = objWMIService.ExecQuery(\"Select * from Win32_Processor\")  \r\n "
					+ "For Each objItem in colItems\r\n " + "    Wscript.Echo objItem.ProcessorId  \r\n "
					+ "    exit for  ' do the first cpu only! \r\n" + "Next                    ";

			fw.write(vbs);
			fw.close();
			String path = file.getPath();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + path);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;
			}
			input.close();
			file.delete();
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		if (result.trim().length() < 1 || result == null) {
			result = "无CPU_ID被读取";
		}
		System.out.println("--------CPU序列号---------:" + result.trim());
		return result.trim();
	}

	public static String pinjie(String a, String b) {
		char[] ar = a.toCharArray();// 6
		char[] br = b.toCharArray();// 12
		StringBuffer c = new StringBuffer();
		for (int i = 0, j = 0; i < ar.length && j < br.length; i++, j++) {
			c.append(ar[i]);
			c.append(br[j++]);
			c.append(br[j]);
		}
		String d = c.toString();
		return d;

	}

}
