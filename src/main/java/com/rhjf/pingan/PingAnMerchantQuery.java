package com.rhjf.pingan;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;

import net.sf.json.JSONObject;

public class PingAnMerchantQuery extends BaseDao {

	/** 商户类请求地址 **/
//	public static final String MERCHANT_URL = "https://testicscbank.pingan.com/qcmerchant/";
	public static final String MERCHANT_URL = "http://172.16.0.211:80/qcmerchant/";

	public static final String SERVICE_CUSTOMER_QUERY = "service.customer.query"; // 查询

//	public static final String companyNo = "20170602002";
	public static final String companyNo = "901170000800";

	/** 签名秘钥 **/
//	public static final String TRADE_MD5_KEY = "O0B0IPKT9WQHIDGW";
	public static final String TRADE_MD5_KEY = "5GLF1UF077A9YX56";

	/** 加密秘钥 **/
//	public static final String TRADE_AES_KEY = "P3NPW94OLI04AP6N";
	public static final String TRADE_AES_KEY = "HID7BTMV1HNCU3FK";

	public void init() {
		
		log.info("查询平安入网没有得到审核结果的商户----");

		String sql = "select * from tbl_pay_merchantchannel where  channelCode='PINGAN' and t0PayResult=99 and channelFlag=1";

//		String sql = "select * from tbl_pay_merchantchannel where  channelCode='PINGAN'  and merchantNo='B100001402'";
		
		
		List<Map<String, Object>> list = queryForList(sql, null);

		log.info("查询没有得到审核结果的商户数量为：" + list.size());
		
		
		for (int i = 0; i < list.size(); i++) {

			Map<String, Object> merchantchannel = list.get(i);
			
			
			String merchantNo = merchantchannel.get("merchantNo").toString();

			String customerNo = merchantchannel.get("superMerchantNo").toString();

			JSONObject requestjson = new JSONObject();
			requestjson.put("customerNo", customerNo);
			
			log.info("加密域代价密：" + requestjson.toString()); 
			

			String encryptData = null;
			try {
				encryptData = Base64.encodeBase64String(
						AesUtil.encryptAES(TRADE_AES_KEY.getBytes("utf-8"), requestjson.toString().getBytes("utf-8")));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Map<String, Object> map = new TreeMap<String, Object>();

			map.put("version", "1.0.0");
			map.put("service", SERVICE_CUSTOMER_QUERY);
			map.put("companyNo", companyNo);
			map.put("encryptData", encryptData);

			String sign = getSignFromJson(JSONObject.fromObject(map), TRADE_MD5_KEY); // 签名
			map.put("signData", sign);

			log.info("请求报文:" + JSONObject.fromObject(map));
			
			String content = HttpClient.post(MERCHANT_URL, map, null);

			log.info(merchantNo + "  ==查询响应报文:" +content);

			try {
				JSONObject repjson = JSONObject.fromObject(content);

				if ("0000".equals(repjson.getString("respCode"))) {

					String respData = repjson.getString("encryptData");

					// 报文AES解密
					String serviceJsonStr = new String(
							AesUtil.decryptAES(TRADE_AES_KEY.getBytes("utf-8"), Base64.decodeBase64(respData)),"utf-8");

					log.info(merchantNo + "  ==查询响应解密报文:" + serviceJsonStr);
					
					JSONObject contentJson = JSONObject.fromObject(serviceJsonStr);

					String status = contentJson.getString("status");

					String id = merchantchannel.get("id").toString();

					if ("TRUE".equals(status)) {
						
						log.info(merchantNo + "  == 通过审核");
						
						// 审核通过
						String update = "update tbl_pay_merchantchannel set channelFlag=0  , t0PayResult = null where id=?";
						executeSql(update, new Object[] { id });
					} else if ("AUDIT_REJECT".equals(status)) {
						
						log.info(merchantNo + "  == 拒绝审核");
						
						// 拒绝审核
						String update = "update tbl_pay_merchantchannel set channelFlag=1  , t0PayResult = 88 where id=?";
						executeSql(update, new Object[] { id });
					}
				} else {
					log.info(merchantNo +" 查询失败");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException, Exception { 
		
		PingAnMerchantQuery merchantquery = new PingAnMerchantQuery();
		merchantquery.init();

	}

	public static String getSignFromJson(JSONObject json, String md5Key) {
		// 签名
		ObjectMapper mapper = new ObjectMapper();
		// 返回数据转换成待签名数据
		TreeMap<String, String> respMap = null;
		try {
			respMap = mapper.readValue(json.toString(), new TypeReference<TreeMap<String, String>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		// 拼接待签名字符串
		for (Map.Entry<String, String> entry : respMap.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue())) {
				sb.append(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		sb.append("key=" + md5Key);
		String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();

		return sign;
	}

}
