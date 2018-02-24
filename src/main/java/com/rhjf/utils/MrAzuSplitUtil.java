package com.rhjf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 截取不规则（空格、符号）工具类
 * @author Zerml
 * @date 2016-6-23 14:52:01
 */
public class MrAzuSplitUtil {

	@SuppressWarnings("rawtypes")
	static List s = new ArrayList();

	/**
	 * @description 参数、需要截取的String和截取符号
	 * @param content
	 * @param sp
	 * @return String[]
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List splitForList(String content, String sp) throws Exception {
		String a = content.trim(); //去掉两边的空格
		String[] b = a.split(sp);  //根据提供参数进行split
		for (String string : b) {
			if (!"".equals(string)) {//提取有效数据
				s.add(string);
			}
		}
		return s;
	}
	
	/**
	 * 
	 * @description 参数、需要截取的String和截取符号
	 * @param content
	 * @param sp
	 * @return String
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String splitForString(String content, String sp)throws Exception {
		String a = content.trim(); //去掉两边的空格
		String[] b = a.split(sp);  //根据提供参数进行split
		for (String string : b) {
			if (!"".equals(string)) {//提取有效数据
				s.add(string);
			}
		}
		StringBuffer sb = new StringBuffer();
		for (String string : b) {
			sb.append(string);
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @description 将提供的String，根据需截取的str,保持几个截取度，并替换成rep
	 * @param content
	 * @param sp
	 * @return String
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String splitForStringByCondition(String content, String sp, String rep, int i)throws Exception {
		String a = content.trim(); //去掉两边的空格
		String[] b = a.split(sp);  //根据提供参数进行split
		int k = 0; //初始化保留截取度
		s.clear();
		for (String string : b) {
			if ("".equals(string)||" ".equals(string)) {//提取有效数据
				if (k>0) {
					continue;
				}
				for (int j = 0; j < i; j++) {
					s.add(rep);
				}
				k++;
			}else{
				s.add(string);
				k=0;
			}
			
		}
		StringBuffer sb = new StringBuffer();
		for (Object str : s) {
			sb.append(str);
		}
		return sb.toString();
	}
	
	/**
	 * @author Zerml
	 * @date 2016年8月31日下午12:45:10
	 * @description 新截取方式，比上面的貌似好用一些
	 */
	@SuppressWarnings("unchecked")
	public static String splitForStringByConditionOther(String content, String sp, String rep)throws Exception {
		String a = content.trim(); //去掉两边的空格
		String[] b = a.split(sp);  //根据提供参数进行split
		int k = 0; //初始化保留截取度
		s.clear();
		for (String string : b) {
			if ("".equals(string)||" ".equals(string)) {//提取有效数据
				if (k>0) {
					continue;
				}
				k++;
			}else{
				s.add(string);
				s.add(rep);
				k=0;
			}
			
		}
		StringBuffer sb = new StringBuffer();
		for (Object str : s) {
			sb.append(str);
		}
		return sb.toString();
	}
	
	/**
	 * 根据字符流读取文件
	 * @param fileName
	 * @param charset
	 * @return
	 */
	public static String readFileByChars(String fileName,String charset) {
		StringBuffer sb = new StringBuffer();
		File file = new File(fileName);
		Reader reader = null;
		try {
			// System.out.println("以字符为单位读取文件内容，一次读一个字节：");
			// 一次读一个字符
			reader = new InputStreamReader(new FileInputStream(file),charset);
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				// 对于windows下，\r\n这两个字符在一起时，表示一个换行。
				// 但如果这两个字符分开显示时，会换两次行。
				// 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
				if (((char) tempchar) != '\r') {
					sb.append((char) tempchar);
					// System.out.print((char) tempchar);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String readFileByChars(InputStream input,String charset) {
		StringBuffer sb = new StringBuffer();
		Reader reader = null;
		try {
			// System.out.println("以字符为单位读取文件内容，一次读一个字节：");
			// 一次读一个字符
			reader = new InputStreamReader(input,charset);
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				// 对于windows下，\r\n这两个字符在一起时，表示一个换行。
				// 但如果这两个字符分开显示时，会换两次行。
				// 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
				if (((char) tempchar) != '\r') {
					sb.append((char) tempchar);
					// System.out.print((char) tempchar);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String getStringForInputStream(InputStream input){
		StringBuffer sb = new StringBuffer();
		byte[] b = new byte[4096];
		try {
			for (int n; (n = input.read(b)) != -1;) {
				sb.append(new String(b, 0, n));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * @author Zerml
	 * @date 2016年11月13日
	 * @description 判断字符串是否为空
	 * @return true为空 false不为空
	 */
	public static boolean isNullOrEmpty(String string) throws Exception{
		try {
			return string == null || string.length() == 0; // string.isEmpty() in Java 6
		} catch (Exception e) { }
		return true;
	 }
	
	//@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		//String str = "明      天     星   期    五     不      上       班";
		String str = "13160321    0619001130      6217994280007646490     邮储银行                          350.00         -1.12            348.88  000001692879  166232        有线销售点终端(P  消费                    ";
		//String str = "00010030    中国银联支付标记               16 6201361      1 4                                 ";
		String a = splitForStringByConditionOther(str, " ","@@@");
		//String a = splitForStringByCondition(str, " ","@@@", 1);
		System.out.println(a);
		String[] aa = a.split("@@@");
		System.out.println("length:"+aa.length);
		for (String string : aa) {
			System.out.println(string);
		}
		
		/*List<String> list = MrAzuSplitUtil.splitForList(str, " ");
		for (String string : list) {
			System.out.print(string);
		}
		System.out.println();
		String strs = " ----  明- 天-  -星   -期    -五 - -   不 -    上   -  -  班-- --   - ";
		String stri = MrAzuSplitUtil.splitForString(strs, "-");
		System.out.println(stri);
		
		String strin = MrAzuSplitUtil.splitForString(stri, " ");
		System.out.println(strin);*/
	}
}
