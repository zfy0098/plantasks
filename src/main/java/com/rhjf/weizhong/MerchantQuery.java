package com.rhjf.weizhong;

import java.io.UnsupportedEncodingException; 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.MD5;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

public class MerchantQuery extends BaseDao{
	
	
//	Jedis jedis = getJedis();
	

	public void init() throws Exception{ 
		
		
		String token  = Utils.getAccessToken();
		
		String ticket = Utils.getTicket(token);
		
		System.out.println("ticket" + ticket); 
		
		String nonce = getNonce();
		
		
		List<String> values = new ArrayList<String>();
		values.add(nonce);
		values.add(Utils.APPID);
		values.add(Utils.VERSION);
		
//		String merchantSQL = "select * from tbl_pay_merchantchannel where  channelCode='WEIZHONG' and t0PayResult=99 and channelFlag=1 limit 1";
		String merchantSQL = "select * from tbl_pay_merchantchannel where  channelCode='WEIZHONG' and t0PayResult=4";
		List<Map<String, Object>> list = queryForList(merchantSQL, null);
		
		for (int i = 0; i < list.size(); i++) {

			Map<String, Object> merchantchannel = list.get(i);
			
			JSONObject json = new JSONObject();
			
			json.put("wbMerchantId", merchantchannel.get("superMerchantNo"));
			json.put("agencyId", Utils.ALIAGENCYID);
			
			values.add(json.toString());
			
			String sign = sign(values, ticket).toUpperCase();
			
			
			String url = "https://l.test-svrapi.webank.com/api/aap/server/wepay/querymerchant?app_id=" + Utils.APPID + "&nonce=" +nonce+ "&version="+Utils.VERSION+"&sign=" + sign;
			
			log.info("json" + JSONObject.fromObject(json)); 
			
			
			System.out.println("商户查询请求地址:" + url);
			
			String content  = Utils.send(url, JSONObject.fromObject(json).toString());
			
			System.out.println(content);
			
			JSONObject respjson = JSONObject.fromObject(content);
			String code =  respjson.getString("code");
			String checkStatus = respjson.getString("checkStatus");
			if("0".equals(code)&&"1".equals(checkStatus)){
				String sql = "update tbl_pay_merchantchannel set t0PayResult=null , channelFlag=0 where ID=?";
				executeSql(sql, new Object[]{merchantchannel.get("ID")}); 
			}
			
		}
		
	}
	
	
	
//	public String getAccessToken() throws Exception{
//		
//		System.out.println("获取token");
//		
//		
//		String accessToken = jedis.get("weizhong_accessToken");
//		if(accessToken == null){
//			
//			String url = Utils.accessTokenURL + "?app_id=" + Utils.APPID + "&secret=" + Utils.SECRET + "&grant_type=client_credential&version=" + Utils.VERSION;
//			
//			String content = Utils.getToken(url);
//			
//			JSONObject json = JSONObject.fromObject(content);
//			
//			System.out.println(json); 
//			
//			if("0".equals(json.getString("code"))){
//				
//				accessToken = json.getString("access_token");
//				jedis.set("weizhong_accessToken", accessToken);
//				jedis.expire("weizhong_accessToken", 7200);
//				
//				return accessToken;
//			}
//		}else{
//			return accessToken;
//		}
//		
//		return null;
//	}
	
//	public String getTicket(String accessToken) throws Exception {
//
//		
//		
//		String value = jedis.get("weizhong_Ticket");
//		
//		if(value == null){
//			
//			String url =  Utils.ticketURL + "?app_id=" + Utils.APPID + "&access_token=" + accessToken + "&type=SIGN&version=" + Utils.VERSION;
//			
//			String content =  Utils.getToken(url);
//			
//			JSONObject json = JSONObject.fromObject(content);
//			
//			System.out.println("获取ticket：" + json);
//			
//			if("0".equals(json.getString("code"))){
//				
//				JSONArray array = json.getJSONArray("tickets");
//				value = array.getJSONObject(0).getString("value");
//				
//				jedis.set("weizhong_Ticket", value);
//				
//				jedis.expire("weizhong_Ticket", 3600);
//				
//				return value;
//			}
//		}else{
//			return value;
//		}
//		
//		System.out.println("getTicket.ticket:" + value);
//		
//		return null;
//	}
	
	public String getNonce(){
		return MD5.sign(UUID.randomUUID().toString(), "UTF-8").toUpperCase();
	}
	
	public String sign(List<String> values , String signTicket) {
		if (values == null) {
			throw new NullPointerException("values is null");
		}
		values.removeAll(Collections.singleton(null));
		values.add(signTicket);
		Collections.sort(values);
		StringBuffer sb = new StringBuffer();
		for (String string : values) {
			sb.append(string);
		}
		try {
			MessageDigest md = MessageDigest.getInstance("sha1");
			md.update(sb.toString().getBytes("utf-8"));
			String sign = bytesToHex(md.digest());
			return sign;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String bytesToHex(byte[] src){  
	    StringBuilder stringBuilder = new StringBuilder("");  
	    if (src == null || src.length <= 0) {  
	        return null;  
	    }  
	    for (int i = 0; i < src.length; i++) {  
	        int v = src[i] & 0xFF;  
	        String hv = Integer.toHexString(v);  
	        if (hv.length() < 2) {  
	            stringBuilder.append(0);  
	        }  
	        stringBuilder.append(hv);  
	    }  
	    return stringBuilder.toString();  
	}  
	
	
	public Jedis getJedis(){
		Jedis jedis = new Jedis("10.10.20.101", 6379);
		jedis.auth("ronghuiredis");
//		jedis.del("weizhong_Ticket");
//		jedis.del("weizhong_accessToken");
		return jedis;
	}
	
	public static void main(String[] args) throws Exception {
		MerchantQuery merchantQuery = new MerchantQuery();
		merchantQuery.init();
	}
	
	
	
}
