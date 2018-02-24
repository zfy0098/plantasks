package com.rhjf.ccb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.rhjf.utils.AESUtil;
import com.rhjf.utils.HttpClient;

import net.sf.json.JSONObject;

/**
 *   建设银行对账工具类
 * @author a
 *
 */
public class CCBCheckAccount {
	
	
	Logger log = Logger.getLogger(this.getClass());

	/** 对账单下载 **/
	private static final String URL = "https://api.jia007.com/api-center/rest/v2.0/yqt/downloadFile";
	
	/** 商户编号 **/
	private static final String MerchatNo = "1051100010000722";
	
	/** 商户秘钥 **/
	private static final String Key = "0bd9016221fa4d3db7fd5b9b9410c51c8709212f605947a08054ee9443ea25cb";
	
	/** 对账单业务类型 **/
	private static final String TRADE = "TRADE";

	
	public void init(){
		
		log.info("开始下载建行对账文件");
		
		String date = getdate();
		Map<String,String> map = new TreeMap<String,String>();
		map.put("checkDate", date);
		map.put("bizType", TRADE);
		
		log.info("请求参数:" + map.toString()); 
		
		String encrypt = AESUtil.encrypt(JSONObject.fromObject(map).toString(), Key.substring(0, 16));
		
		Map<String,Object> params = new HashMap<String,Object>();
		
		params.put("appKey", MerchatNo);
		params.put("data", encrypt);
		
		log.info("加密参数:" + params); 
		
		String content = HttpClient.post(URL, params, "1");

		log.info("响应报文:" + content); 
		try {
			File file = new File("/opt/sh/" + date + ".csv");
			byte[] bytes = content.getBytes("utf-8");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public void readfile(){
		File csv = new File("e:/2222.csv"); // CSV文件路径
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csv));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = "";
		String everyLine = "";
		try {
			List<String> allString = new ArrayList<String>();
			while ((line = br.readLine()) != null) // 读取到的内容给line变量
			{
				everyLine = line;
				System.out.println(everyLine);
				allString.add(everyLine);
			}
			System.out.println("csv表格中所有行数：" + allString.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getdate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		
		date = "2017-06-01";
		
		return date;
	}
	
	
	public static void main(String[] args) {
		CCBCheckAccount ccb = new CCBCheckAccount();
		ccb.init();
	}
	
}
