package com.rhjf.unionpaydf;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import cfca.sadk.algorithm.common.PKIException;

public class TJDF {

		public static void main(String[] args) {
			execute23();
		}
		
		public static boolean execute23() {
			System.out.println("main 方法执行了");
			Map<String, String> signature = null;
			String date = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			//onlineOrder.setWithdrawTime(date);
			Map<String, String> map = new HashMap<String,String>();
			map.put("version", "1.0");
			map.put("txnType", "12");
			map.put("merId", "309113148162011");
			map.put("settType", "0");
			map.put("oldMerid", "B1000026");
			map.put("signMethod", "1");
			map.put("txnTime", date);
			map.put("accNo", "130823199105284010");
			map.put("backUrl", "");
			map.put("txnAmt", "1");
			map.put("enterpriseNo", "80001");
			map.put("orderId","1561516515");
			map.put("bankNo", "6214680002187654");
			map.put("BankName", "北京银行");
			map.put("payName", "张志国");
			map.put("ppType", "0");

			boolean checkSign =false;
			try {
				String res = UnionPayUtil.unionPayExecpayment(map);
				//logger.info("===银联返回的报文==="+res);
				System.out.println("===银联返回的报文==="+res);
				JSONObject obj = JSONObject.parseObject(res);
				Map<String, String> contentData = parserToMap(obj.toString());
				try {
					checkSign = UnionPayUtil.checkSign(contentData);
				} catch (PKIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("yanqinajieguo::==="+checkSign);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//logger.info("---向银联发送的报文---"+signature);
			
	        return checkSign;
			
		}
		
		public static HashMap<String, String> parserToMap(String s) {
			HashMap<String, String> map = new HashMap<String, String>();
			JSONObject json = JSONObject.parseObject(s);
			Iterator it = json.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value;
				try {
					value = json.getString(key);
				} catch (JSONException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				map.put(key, value);
			}
			return map;
		}
}
