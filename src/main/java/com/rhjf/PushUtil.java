package com.rhjf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;

import com.rhjf.utils.HttpClient;
import com.rhjf.utils.MD5;

import net.sf.json.JSONObject;


public class PushUtil {
	
	private String url = "http://msg.umeng.com/api/send";
	private String appkey = "59ad1251c62dca7ccb00071a";
	private String appMasterSecret = "ezcfogsow6bysilnh0lssm1yixwhbijv";

	
	private String androidAppkey = "59ae7454310c93467d000288";
	
	private String androidappMasterSecret = "psivnf3hwzzmgfivd1rfj2zktt5acay9";
	
	public void iosSend(){
	
		JSONObject json = new JSONObject();
		
		
		json.put("timestamp", System.currentTimeMillis());
		json.put("production_mode" , "false");
		json.put("appkey", appkey);
		
		
		/**
		 * ,73fa12de981d969df41427222076cd4c0de2f0daf094888f91a3f86175f508fd,"
				+ "d168112313924dc753069a0c5e9f1036dc595ae0179d62e6d0c331d78485050e"
		 */
		
		JSONObject payload = new JSONObject();
		
		JSONObject aps = new JSONObject();

		aps.put("sound", "default");
		aps.put("content-available", 1);
		aps.put("badge", 1);
		
		
		JSONObject alert = new JSONObject();
		alert.put("title", "IOS单播测试");
		alert.put("subtitle", "这是自标题");
		alert.put("body", "这是一个内容");
		aps.put("alert", alert);
		
		
		payload.put("aps", aps);
		
		payload.put("type", "2");
		
		json.put("payload", payload);
		
		json.put("device_tokens", "2c5b76d2997118801772fa1315dd14c020a12843cee864b013fbdc940cab655d");
		
		json.put("type", "unicast");
		
        String sign = null;
		try {
			System.out.println(("POST" + url + json.toString() + appMasterSecret)); 
			sign = DigestUtils.md5Hex(("POST" + url + json.toString() + appMasterSecret).getBytes("utf8"));
			
			System.out.println(sign);
			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		url = url + "?sign=" + sign;
		
		try {
			System.out.println(json.toString()); 
			String content = HttpClient.xml(url, json.toString());
			System.err.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void androidSend(){
		JSONObject json = new JSONObject();
		json.put("appkey", androidAppkey);
		json.put("timestamp", System.currentTimeMillis());
		json.put("type", "listcast");
		json.put("device_tokens", "AlDXcAtK72x4-hV2v9Jf8oCGIKDB6QTVNhLS7aWvfNOd");
		
		JSONObject payload = new JSONObject();
		payload.put("display_type", "notification");  //  notification-通知，message-消息
		
		JSONObject body = new JSONObject();
		body.put("ticker", "ticker中文");
		body.put("title", "title中文");
		body.put("text", "尼日尔同");
		
		body.put("after_open", "go_custom");
		
		body.put("custom", "123123");
		
		payload.put("body", body);
		
		json.put("payload", payload);
		
		json.put("production_mode", "false");
		json.put("description", "description");
		
		
		String sign = MD5.sign("POST" + url + json.toString() + androidappMasterSecret, "utf-8");
		
		url = url + "?sign=" + sign;
		
		try {
			String content = HttpClient.xml(url, json.toString());
			System.err.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		PushUtil push = new PushUtil();
//		push.iosSend();
		push.androidSend();
	}
	
	
	
	
	
	

}
