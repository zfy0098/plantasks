package com.rhjf.weizhong;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.rhjf.utils.MD5;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

public class Utils {
	
	
	private static Jedis jedis;
	
	public static final String APPID = "W1055976";
	
	public static final String VERSION = "1.0.0";
	
	public static final String SECRET = "pOX71twsSXayVymAwS2QnhK0eJloMlP7nCG77LQN9VqKCAzqk5d3eTPUEkRUkIjs";
	
	/** 微信代理商ID **/
	public static final String WXAGENCYID = "2021000362";
	
	/** 支付宝代理商ID **/
	public static final String ALIAGENCYID = "2031000290";
	
	
	/**  获取 accessTokenURL **/
	public static String accessTokenURL = "https://l.test-svrapi.webank.com/api/oauth2/access_token/";
	
	/**  获取ticke **/
	public static String ticketURL = "https://l.test-svrapi.webank.com/api/oauth2/api_ticket";
	
	
	/**  创建子商户配置 **/
	public static String merchantConfig = "https://l.test-svrapi.webank.com/api/aap/server/wepay/addsubdevconfig";
	
	
	public static String getAccessToken() throws Exception{ 
		
		Jedis jedis = getJedis();
		
		String accessToken = jedis.get("weizhong_accessToken");
		if(accessToken == null){
			
			String url = accessTokenURL + "?app_id=" + APPID + "&secret=" + SECRET + "&grant_type=client_credential&version=" + VERSION;
			
			String content = getToken(url);
			
			JSONObject json = JSONObject.fromObject(content);
			
			if("0".equals(json.getString("code"))){
				
				accessToken = json.getString("access_token");
				jedis.set("weizhong_accessToken", accessToken);
				jedis.expire("weizhong_accessToken", 7200);
				
				return accessToken;
			}
		}else{
			return accessToken;
		}
		
		return null;
	}
	
	public static  String getTicket(String accessToken) throws Exception {
		
		Jedis jedis = getJedis();
		
		String value = jedis.get("weizhong_Ticket");
		
		if(value == null){
			
			String url = ticketURL + "?app_id=" + APPID + "&access_token=" + accessToken + "&type=SIGN&version=" + VERSION;
			
			String content = getToken(url);
			
			JSONObject json = JSONObject.fromObject(content);
			
			if("0".equals(json.getString("code"))){
				
				JSONArray array = json.getJSONArray("tickets");
				value = array.getJSONObject(0).getString("value");
				
				jedis.set("weizhong_Ticket", value);
				
				jedis.expire("weizhong_Ticket", 3600);
				
				return value;
			}
		}else{
			return value;
		}
		return null;
	}
	
	public static String getNonce(){
		return MD5.sign(UUID.randomUUID().toString(), "UTF-8").toUpperCase();
	}
	
	public static String sign(List<String> values , String signTicket) {
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
	
	public static String bytesToHex(byte[] src){  
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
	
	
	public synchronized static Jedis getJedis(){
		if(jedis == null){
			jedis = new Jedis("10.10.20.101", 6379);
			jedis.auth("ronghuiredis");
		}
		return jedis;
	}
	
	

	public static void removeRedis(){
		jedis.del("weizhong_Ticket");
		jedis.del("weizhong_accessToken");
	}
	
	
	
	public static String send(String webUrl, String xmlFile) throws Exception {

		HttpPost httpPost = new HttpPost(webUrl);

        
		if(!StringUtils.isEmpty(xmlFile)){
			StringEntity reqentity = new StringEntity(new String(xmlFile.getBytes("UTF-8")), "UTF-8");

	 	    reqentity.setContentType("application/json;charset=UTF-8");

			httpPost.setEntity(reqentity);
		}
		
		
		////////////////////////////////////////////////////////////////
		// 此处加入https双向认证
		String KEY_STORE_PASSWORD = "Abcd1234";
		String KEY_STORE_TRUST_PASSWORD = "123456";
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		KeyStore trustStore = KeyStore.getInstance("JKS");

		InputStream ksIn = new FileInputStream("/key/www.crvc.org.cn.p12");
		InputStream tsIn = new FileInputStream("/key/webank_truststore.jks");

		try {
			keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
			trustStore.load(tsIn, KEY_STORE_TRUST_PASSWORD.toCharArray());
		} catch (Exception e) {
			System.out.println("got a exception" + e.getMessage());
		} finally {
			ksIn.close();
			tsIn.close();
		}

		SSLContext sslcontext = null;
		sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, KEY_STORE_PASSWORD.toCharArray())
				.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
				null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		HttpResponse response = httpclient.execute(httpPost);

		HttpEntity entity = response.getEntity();

		String respContent = EntityUtils.toString(entity, "UTF-8").trim();
		
		return respContent;
	}
	
	
	public static String getToken(String webUrl) throws Exception {
		HttpGet get = new HttpGet(webUrl);
		System.out.println("发送地址：" + webUrl);
		
		////////////////////////////////////////////////////////////////
		// 此处加入https双向认证
		String KEY_STORE_PASSWORD = "Abcd1234";
		String KEY_STORE_TRUST_PASSWORD = "123456";
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		KeyStore trustStore = KeyStore.getInstance("JKS");
		
		InputStream ksIn = new FileInputStream("/key/www.crvc.org.cn.p12");
		InputStream tsIn = new FileInputStream("/key/webank_truststore.jks");
		
		try {
		keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
		trustStore.load(tsIn, KEY_STORE_TRUST_PASSWORD.toCharArray());
		} catch (Exception e) {
		System.out.println("got a exception" + e.getMessage());
		} finally {
			ksIn.close();
			tsIn.close();
		}
		
		SSLContext sslcontext = null;
		sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, KEY_STORE_PASSWORD.toCharArray())
		.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
		
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
		null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		
		HttpResponse response = httpclient.execute(get);
		
		System.out.println(get.getURI());
		
		HttpEntity entity = response.getEntity();
		
		String respContent = EntityUtils.toString(entity, "UTF-8").trim();
		
		return respContent;
		
	}
}
