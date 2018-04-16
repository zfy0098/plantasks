package com.rhjf.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

public class HttpClient {

	
	private static  Logger logtool = Logger.getLogger(HttpClient.class);
	
	/**
	 *     发送http post请求
	 * @param callURL   请求的目标地址
	 * @param resultMap 请求参数
	 * @param paramtype 参数格式：1位键值对 key-value形式 其他为json格式
	 * @return
	 */
	public static String post(String callURL, Map<String,Object> resultMap,String paramtype){ 

		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(400000).setConnectTimeout(400000)
				.setSocketTimeout(400000).setExpectContinueEnabled(false)
				.build();

		HttpPost httppost = new HttpPost(callURL);

		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();// 设置进去

		if(resultMap!=null){
			if(paramtype!=null&&paramtype.equals("1")){
				//  key value 形式
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {  
					  
				    formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString())); 
				}  
				try {
					UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
					httppost.setEntity(uefEntity);
				} catch (UnsupportedEncodingException e) {
					System.out.println(e.getMessage());
					logtool.error(e.getMessage());
					return "";
				} 
			}else{
				StringEntity rsqentity = new StringEntity(JSONObject.fromObject(resultMap).toString(), "utf-8");
				rsqentity.setContentEncoding("UTF-8");
				rsqentity.setContentType("application/json");
				httppost.setEntity(rsqentity);
			}
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httppost);

			HttpEntity rspentity = response.getEntity();
			InputStream in = rspentity.getContent();

			String temp;
			BufferedReader data = new BufferedReader(new InputStreamReader(in, "utf-8"));
			StringBuffer result = new StringBuffer();
			while ((temp = data.readLine()) != null) {
				result.append(temp);
				temp = null;
			}
			return result.toString();
		} catch (ClientProtocolException e) {
			logtool.error(e.getMessage());
		} catch (IllegalStateException e) {
			logtool.error(e.getMessage());
		} catch (IOException e) {
			logtool.error(e.getMessage());
		} finally {
			try {
				response.close();
				httpClient.close();
			} catch (IOException e) {
				logtool.error(e.getMessage());
			}
		}
		return null;
	}
	
	
	/**
	 *     发送http post请求
	 * @param callURL   请求的目标地址
	 * @param resultMap 请求参数
	 * @param paramtype 参数格式：1位键值对 key-value形式 其他为json格式
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String post(String callURL, Map<String,Object> header ,  Map<String,Object> resultMap,String paramtype) 
			throws ClientProtocolException, IOException{ 

		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(400000).setConnectTimeout(400000)
				.setSocketTimeout(400000).setExpectContinueEnabled(false)
				.build();

		HttpPost httppost = new HttpPost(callURL);
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

		/** 頭信息 **/
		if (header != null) {
			for (Map.Entry<String, Object> entry : header.entrySet()) {
				httppost.addHeader(entry.getKey(), entry.getValue().toString());
			}
		}

		if (resultMap != null) {
			if (paramtype != null && paramtype.equals("1")) {
				// key value 形式
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
				httppost.setEntity(uefEntity);
			} else {
				StringEntity rsqentity = new StringEntity(JSONObject.fromObject(resultMap).toString(), "utf-8");
				rsqentity.setContentEncoding("UTF-8");
				rsqentity.setContentType("application/json");
				httppost.setEntity(rsqentity);
			}
		}
		CloseableHttpResponse response = null;
		response = httpClient.execute(httppost);

		HttpEntity rspentity = response.getEntity();
		InputStream in = rspentity.getContent();

		String temp;
		BufferedReader data = new BufferedReader(new InputStreamReader(in, "utf-8"));
		StringBuffer result = new StringBuffer();
		while ((temp = data.readLine()) != null) {
			result.append(temp);
			temp = null;
		}

		response.close();
		httpClient.close();
		return result.toString();
	}
	
	
	public static String xml(String callURL, String xmlData) throws ClientProtocolException, IOException {
		
		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(400000).setConnectTimeout(400000)
				.setSocketTimeout(400000).setExpectContinueEnabled(false)
				.build();

		HttpPost httppost = new HttpPost(callURL);
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();// 设置进去

		StringEntity entity = new StringEntity(xmlData, "UTF-8");

		httppost.setHeader("User-Agent", "Mozilla/5.0");

		httppost.setEntity(entity);
		CloseableHttpResponse response = null;
		response = httpClient.execute(httppost);

		HttpEntity rspentity = response.getEntity();
		InputStream in = rspentity.getContent();

		String temp;
		BufferedReader data = new BufferedReader(new InputStreamReader(in, "utf-8"));
		StringBuffer result = new StringBuffer();
		while ((temp = data.readLine()) != null) {
			result.append(temp);
			temp = null;
		}
		response.close();
		httpClient.close();
		return result.toString();
	}
}
