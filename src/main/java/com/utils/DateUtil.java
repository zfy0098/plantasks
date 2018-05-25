package com.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

	public static String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	public static String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

	public static String yyyyMMdd = "yyyyMMdd";
	
	public static String HHmmss = "HHmmss";
	
	public static String HHmm = "HH-mm";

	public static String yyyy_MM_dd = "yyyy-MM-dd";
	

	/**
	 *   获取当前时间时间
	 * @param format
	 * @return
	 */
	public static String getNowTime(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
		
	}
	public static String getDateTimeTemp() {
		java.text.SimpleDateFormat d = new java.text.SimpleDateFormat();
		d.applyPattern(yyyyMMddHHmmss);
		java.util.Date nowdate = new java.util.Date();
		String str_date = d.format(nowdate);
		return str_date;
	}
	

	/**
	 *    获取指定当天日之前的几天的日期
	 * @param day
	 * @return
	 */
	public static String getDateAgo(String dateStr , int day , String format) throws Exception{
		Calendar calendar1 = Calendar.getInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat(format);
		Date date = sdf1.parse(dateStr);//初始日期
		calendar1.setTime(date);
		calendar1.add(Calendar.DATE, -day);
		String daysAgo = sdf1.format(calendar1.getTime());
		return  daysAgo;
	}

	public static String parseToStr(String sDate, String pattern) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(pattern);
		return sf.format(sDate);
	}


	public static List<String> getBetweenDates(String startTime , String endTime ,String  format) throws Exception{
		
		List<String> dataList = new ArrayList<>();
		dataList.add(startTime);

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dBegin = sdf.parse(startTime);
		Date dEnd = sdf.parse(endTime);

		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calBegin.setTime(dBegin);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calEnd.setTime(dEnd);
		// 测试此日期是否在指定日期之后
		while (dEnd.after(calBegin.getTime())) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			dataList.add(sdf.format(calBegin.getTime()));
		}
		return dataList;
	}
	
}
