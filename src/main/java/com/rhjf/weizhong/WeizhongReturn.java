package com.rhjf.weizhong;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

public class WeizhongReturn {

	Logger log = Logger.getLogger(this.getClass());
	
	public void init(){
		JSONObject json = new JSONObject();
		
		json.put("wxMerchantId", "202002427422158");
		json.put("orderId", UUID.randomUUID().toString().toUpperCase());
		json.put("refundNo", "WEIZHONG1500966836241bJh2C39");
		json.put("refundAmount", "1000");
		
		String nonce = Utils.getNonce();
		
		List<String> values  = new ArrayList<String>();
		values.add(Utils.APPID);
		values.add(Utils.VERSION);
		values.add(nonce);
		values.add(json.toString());
		
		String sign = null;
		try {
			String signTicket = Utils.getTicket(Utils.getAccessToken());
			sign = Utils.sign(values, signTicket);
		}catch (Exception e2) {
			e2.printStackTrace();
		}
		
		String url = "https://l.test-svrapi.webank.com/api/aap/server/wepay/refund?app_id=" + Utils.APPID + "&nonce=" +nonce+ "&version="+Utils.VERSION+"&sign=" + sign;
		
		
		log.info("微众请求地址:" + url);
		log.info("微众银行请求数据:" + json.toString()); 
		
		String content;
		try {
			content = Utils.send(url, json.toString());
			log.info("微众银行响应数据" + content); 
			
			net.sf.json.JSONObject repjson = net.sf.json.JSONObject.fromObject(content);
			
			if ("0".equals(repjson.getString("code"))) {
			} else if ("400101".equals(json.getString("code"))) {
				log.info("疑发签名错误异常，删除redis中的缓存数据");
				Utils.removeRedis();
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
