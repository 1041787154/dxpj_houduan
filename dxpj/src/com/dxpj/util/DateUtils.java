package com.dxpj.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat dateTimeFormat1 = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat dateTimeFormat2 = new SimpleDateFormat("MMddyyyyHHmmss");
	
	public static String dateToStr(Date date){
		return dateTimeFormat.format(date);
	}
	public static String simDateToStr(Date date){
		return dateTimeFormat1.format(date);
	}
	
	public static String getNowDateTimeStr() {
		return dateTimeFormat.format(new Date());
	}
	public static String getNowDateTimeStr1() {
		return dateTimeFormat1.format(new Date());
	}
	public static String getNowDateTimeStr2() {
		return dateTimeFormat2.format(new Date());
	}
}
