package com.rhjf.weizhong;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.rhjf.base.BaseDao;

public class MerchantConfig extends BaseDao{

	public static final String subAppid = "wx6aedec8f8ba79a9e";
	public static final String jsappPath = "http://trx.ronghuijinfubj.com/middlepaytrx/wx/redirect/";
	
	Logger log = Logger.getLogger(this.getClass());
	
	
	public void init(){
		
		
		log.info("开始配置微众 商户配置信息");
		
		String sql = "select * from tbl_pay_merchantchannel where channelCode='WEIZHONG' and merchantNo='B100001376' and t0PayResult=1";
		
		List<Map<String,String>> list = queryForList(sql);
	
		for (int j = 0; j < list.size(); j++) {
			for (int i = 0; i < 3; i++) {
				JSONObject json = new JSONObject();
				json.put("wbMerchantId", list.get(j).get("superMerchantNo"));
				json.put("agencyId", Utils.WXAGENCYID);
				String text = "";
				
				if(i == 0){
					json.put("jsapiPath", jsappPath);
					text = "jsapiPath";
				}else if(i == 1){
					json.put("subAppid", subAppid);
					text = "subAppid";
				}else if(i == 2){
					json.put("subscribeAppid", subAppid);
					text = "subscribeAppid";
				}else{
					continue;
				}
				
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
				
				String url = Utils.merchantConfig + "?app_id=" + Utils.APPID + "&nonce=" +nonce+ "&version="+Utils.VERSION+"&sign=" + sign;
				
				log.info("微众请求地址:" + url);
				log.info("微众银行请求数据:" + json.toString()); 
				
				String content;
				try {
					content = Utils.send(url, json.toString());
					log.info("微众银行响应数据" + content); 
					
					net.sf.json.JSONObject repjson = net.sf.json.JSONObject.fromObject(content);
					
					if ("0".equals(repjson.getString("code"))) {
						log.info("商户号:" + list.get(j).get("merchantNo") + " , 上游商户号: " + list.get(j).get("superMerchantNo") + "，配置 " + text + "成功");
					} else if ("400101".equals(json.getString("code"))) {
						log.info("疑发签名错误异常，删除redis中的缓存数据");
						Utils.removeRedis();
						i = i-1;
					} else {
						log.info("商户号:" + list.get(j).get("merchantNo") + " , 上游商户号: " + list.get(j).get("superMerchantNo") + "，配置 " + text +
								"失败 ,  失败原因：  " + repjson.getString("msg") );
//						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		MerchantConfig config = new MerchantConfig();
		config.init();
	}
}
