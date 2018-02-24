package com.rhjf.pingan;

import java.io.IOException; 
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PingAnMerchantModify extends BaseDao {
	
	Logger log = Logger.getLogger(this.getClass());
	
	/** 商户类请求地址 **/
//	public static final String MERCHANT_URL = "https://testicscbank.pingan.com/qcmerchant/";
	public static final String MERCHANT_URL = "http://172.16.0.211:80/qcmerchant/";

	public static final String SERVICE_CUSTOMER_modify = "service.customer.modify"; 

//	public static final String companyNo = "20170602002";
	public static final String companyNo = "901170000800";
	
	/** 签名秘钥 **/
//	public static final String TRADE_MD5_KEY = "O0B0IPKT9WQHIDGW";
	public static final String TRADE_MD5_KEY = "5GLF1UF077A9YX56";

	/** 加密秘钥 **/
//	public static final String TRADE_AES_KEY = "P3NPW94OLI04AP6N";
	public static final String TRADE_AES_KEY = "HID7BTMV1HNCU3FK";

	
	public void init(String merchantno) {
		
		log.info("修改商户");

		String sql = "select merchantNo , superMerchantNo from tbl_pay_merchantchannel where  channelCode='PINGAN' and merchantNo=?";

		List<Map<String, Object>> list = queryForList(sql, new Object[]{merchantno});

		for (int i = 0; i < list.size(); i++) {

			Map<String, Object> merchantChannel = list.get(i);
			
			String merchantNo = merchantChannel.get("merchantNo").toString();
			
			String merchantSQL = "select id , merchantNo , signName , showName, legalPerson , address, linkPhone,legalPersonID from tbl_pay_merchant where merchantno=?";
			Map<String, Object> merchant = queryForMap(merchantSQL, new Object[]{merchantNo});
			
			
			String merchantbankinfo = "select * from tbl_pay_merchantbankcard where ownerId=?";
			
			Map<String,Object> merchantbank = queryForMap(merchantbankinfo, new Object[]{merchant.get("id")}); 

			JSONObject params = new JSONObject();
			
			params.put("customerNo", merchantChannel.get("superMerchantNo"));
			params.put("shortName", merchant.get("showName"));
			params.put("address", merchant.get("address"));
			params.put("servicePhone", merchant.get("linkPhone"));
			
			params.put("contactMobile", merchant.get("linkPhone"));
			params.put("accNo", merchantbank.get("accountNo"));
			params.put("accType", "1");
			params.put("bankIdentifying", "1");
//			params.put("notifyUrl", "http://10.14.47.103:11124/middlepaytrx/online/merchantExamineNotify/PINGAN"); 
			params.put("notifyUrl", "http://10.2.180.103:11124/middlepaytrx/online/merchantExamineNotify/PINGAN");
			
			
//			String file3 = "" , file4 = "" , file5 = "";
//			//   HandheldIDPhoto , IDCardFrontPhoto , IDCardReversePhoto
//			String zhengmian = "http://10.10.20.101:11024/web/B100171922/zhengmian.jpg";
//			String fanmian = "http://10.10.20.101:11024/web/B100171922/fanmian.jpg";
//			String shouchi = "http://10.10.20.101:11024/web/B100171922/shouchi.jpg";
//			
//			try {
//				file3 = URLImage64Bit.encodeImgageToBase64(new URL(zhengmian)); 
//				file4 = URLImage64Bit.encodeImgageToBase64(new URL(fanmian)); 
//				file5 = URLImage64Bit.encodeImgageToBase64(new URL(shouchi));
//			} catch (MalformedURLException e2) {
//				e2.printStackTrace();
//			} 
//			params.put("file3", file3); // 身份证正面
//			params.put("file4", file4);	// 身份证背面
//			params.put("file5", file5);	// 手持身份证背面
			
			
			List<Map<String,Object>> feelist = queryForList("SELECT * FROM tbl_online_product_fee where merchantNo=?", new Object[]{merchant.get("merchantNo")});
			
			
			int wxlength = PingAnMerchantReport.wxMCCType.length;
			Random random = new Random(wxlength-1);
			int index = random.nextInt(wxlength-1);
			Integer cccNumber = PingAnMerchantReport.wxMCCType[index];
			
			
			JSONArray payTypeContentArray = new JSONArray();
			for (int j = 0; j < feelist.size(); j++) {
				
				Map<String,Object> feemap = feelist.get(j);
				
				JSONObject payType = new JSONObject();
				
				if("WX".equals(feemap.get("bankId"))){
					payType.put("payType", PingAnMerchantReport.WECHAT);
					payType.put("category", cccNumber.toString());
				}else if("Alipay".equals(feemap.get("bankId"))){
					
					String alimcc = PingAnMerchantReport.mccmap.get(cccNumber);
					
					payType.put("payType", PingAnMerchantReport.ALIPAY);
					payType.put("category", alimcc);
				} else {
					continue;
				}
				
				payType.put("t0TradeRate", feemap.get("t0Fee").toString());
				payType.put("t0WithdrawFee", "0");
				payType.put("t0ExemptionAmount", "10000");
				payType.put("t1TradeRate", feemap.get("value").toString());
				payTypeContentArray.add(payType);
			}
			
			params.put("payTypeContent", payTypeContentArray);    //支付方式集合
			
			log.info("加密域代代码：" + params.toString()); 
			
			
			String encryptData = null;
			try {
				encryptData = Base64.encodeBase64String(AesUtil.encryptAES(TRADE_AES_KEY.getBytes("utf-8"), params.toString().getBytes("utf-8")));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Map<String, Object> map = new TreeMap<String, Object>();

			map.put("version", "1.0.0");
			map.put("service", SERVICE_CUSTOMER_modify);
			map.put("companyNo", companyNo);
			map.put("encryptData", encryptData);

			String sign = getSignFromJson(JSONObject.fromObject(map), TRADE_MD5_KEY); // 签名
			map.put("signData", sign);

			log.info("请求报文:" + JSONObject.fromObject(map));
			
			String content = HttpClient.post(MERCHANT_URL, map, null);

			log.info(merchantNo + "  ==修改响应报文:" +content);

			try {
				JSONObject repjson = JSONObject.fromObject(content);

				if ("0000".equals(repjson.getString("respCode"))) {

					String respData = repjson.getString("encryptData");

					// 报文AES解密
					String serviceJsonStr = new String(AesUtil.decryptAES(TRADE_AES_KEY.getBytes("utf-8"), Base64.decodeBase64(respData)),"utf-8");

					log.info(merchantNo + "  == 修改响应解密报文:" + serviceJsonStr);
					
					JSONObject contentJson = JSONObject.fromObject(serviceJsonStr);
					
					System.out.println("申请成功" + contentJson) ;
				} 
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		
		
		if(args.length < 1){
			System.out.println("请输入商户号");
			return ;
		}
		String merchantno = args[0];
		
		PingAnMerchantModify merchantmodify = new PingAnMerchantModify();
		merchantmodify.init(merchantno);

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
