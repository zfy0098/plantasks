package com.rhjf.pingan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.rhjf.utils.CreateExcle;
import com.rhjf.utils.HttpClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PingAnMerchantReport extends BaseDao {

	Logger log = Logger.getLogger(this.getClass());

	/** 商户类请求地址 **/
	// public static final String MERCHANT_URL = "http://172.17.16.124:80/qcmerchant/";
	// public static final String MERCHANT_URL = "https://testicscbank.pingan.com/qcmerchant/";
	public static final String MERCHANT_URL = "http://172.16.0.211:80/qcmerchant/";

	public static final String SERVICE_CUSTOMER_modify = "service.customer.register"; // 查询

	// public static final String companyNo = "20170602002";
	public static final String companyNo = "901170000800";

	/** 支付方式 **/
	public static final String ALIPAY = "ALIPAY";
	public static final String WECHAT = "WECHAT";

	/** 签名秘钥 **/
	// public static final String TRADE_MD5_KEY = "O0B0IPKT9WQHIDGW";
	public static final String TRADE_MD5_KEY = "5GLF1UF077A9YX56";

	/** 加密秘钥 **/
	// public static final String TRADE_AES_KEY = "P3NPW94OLI04AP6N";
	public static final String TRADE_AES_KEY = "HID7BTMV1HNCU3FK";

	public void init(String[] args) {

		Long startTime = System.currentTimeMillis();

//		String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b on a.id=b.ownerId"
//				+ " INNER JOIN (select merchantNo from tbl_online_order where date(createDate)=date(DATE_ADD(now(),INTERVAL -1 day)) and "
//				+ " merchantNo not in (select merchantNo from tbl_pay_merchantchannel where channelCode='PINGAN') GROUP BY merchantNo) as c"
//				+ " where a.merchantStatus='AVAILABLE' and (a.channelApplyMsg not like 'PINGAN%' or a.channelApplyMsg is null ) and a.merchantNo=c.merchantNo";
//
//		if (args.length > 0) {
//			sql = "select a.* , b.* FROM tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b on a.id=b.ownerId "
//					+ "and merchantno='" + args[0] + "'";
//			log.info("输入参数：将上报指定商户号:" + args[0] + ",执行sql:" + sql);
//		} else {
//			log.info("没有指定参数 ，上报集合商户，执行sql ：" + sql);
//		}
		
		String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b on a.id=b.ownerId "
				+ "where a.merchantStatus='AVAILABLE' and  (a.channelApplyMsg not like 'PINGAN%' or a.channelApplyMsg is null ) and agent_id='190'";
		

		List<Map<String, Object>> list = queryForList(sql, null);

		List<Object[]> alist = new ArrayList<Object[]>();
		String[] title = { "商户号", "商户名称", "入网结果" };

		int successCount = 0;
		int failCount = 0;

		for (int j = 0; j < list.size(); j++) {

			Map<String, Object> merchant = list.get(j);

			String merchantNo = merchant.get("merchantNo").toString();

			JSONObject params = new JSONObject();

			if (!merchant.get("merchantType").equals("PERSON")) {
				log.info(" ### 平安入网 ### 商户号:" + merchant.get("merchantNo") + "非个人商户，停止入网");

				failCount += 1;

				String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
				executeSql(upmerchantinfoup, new Object[] { "PINGAN非个人商户，停止入网", merchantNo });

				log.info("商户：" + merchantNo + "在平安入网失败,原因：非个人商户，停止入网");
				Object[] obj = new Object[] { merchantNo, merchant.get("showName"), "非个人商户，停止入网" };
				alist.add(obj);

				continue;
			}

			params.put("comCustomerNo", merchant.get("merchantNo"));
			params.put("fullName", merchant.get("legalPerson"));
			params.put("shortName", merchant.get("showName"));
			params.put("customerType", "0"); // 商户类型 0 个人 1 企业、个体户
			params.put("servicePhone", merchant.get("linkPhone"));
			params.put("contactMobile", merchant.get("linkPhone"));

			params.put("idType", "0");
			params.put("idNo", merchant.get("legalPersonID"));

			params.put("accNo", merchant.get("accountNo"));
			params.put("accName", merchant.get("accountName"));
			params.put("accType", "1");

			params.put("bankIdentifying", "1");
			params.put("notifyUrl", "http://10.2.180.103:11124/middlepaytrx/online/merchantExamineNotify/PINGAN");

			List<Map<String, Object>> feelist = queryForList("SELECT * FROM tbl_online_product_fee where merchantNo=?", new Object[] { merchant.get("merchantNo") });

			int wxlength = wxMCCType.length;
			Random random = new Random(wxlength - 1);
			int index = random.nextInt(wxlength - 1);
			Integer cccNumber = wxMCCType[index];

			JSONArray payTypeContentArray = new JSONArray();
			for (int i = 0; i < feelist.size(); i++) {
				Map<String, Object> feemap = feelist.get(i);

				JSONObject payType = new JSONObject();

				if ("WX".equals(feemap.get("bankId"))) {
					payType.put("payType", WECHAT);
					payType.put("category", cccNumber.toString());
				} else if ("Alipay".equals(feemap.get("bankId"))) {
					String alimcc = mccmap.get(cccNumber);
					payType.put("payType", ALIPAY);
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

			System.out.println(payTypeContentArray.toString());

			params.put("payTypeContent", payTypeContentArray); // 支付方式集合

			log.info(merchantNo + "商户平安入网请求明文：" + params.toString());

			String encryptData = null;
			try {
				encryptData = Base64.encodeBase64String(
						AesUtil.encryptAES(TRADE_AES_KEY.getBytes("utf-8"), params.toString().getBytes("utf-8")));
			} catch (Exception e1) {
				log.info(merchantNo + " ### 平安入网  数据加密错误 ###   =====" + e1.getMessage());
				failCount += 1;
				Object[] obj = new Object[] { merchantNo, merchant.get("showName"), "数据加密错误:" + e1.getMessage() };
				alist.add(obj);
				continue;
			}

			Map<String, Object> json = new TreeMap<String, Object>();

			json.put("version", "1.0.0");
			json.put("service", "service.customer.register");
			json.put("companyNo", companyNo);
			json.put("encryptData", encryptData);

			String sign = getSignFromJson(JSONObject.fromObject(json), TRADE_MD5_KEY); // 签名

			json.put("signData", sign);

			log.info(merchantNo + "商户平安入网请求完整数据：" + json.toString());

			String content;
			try {
				content = HttpClient.post(MERCHANT_URL, json, null);

				log.info(merchantNo + "平安注册响应响应:" + content);

				net.sf.json.JSONObject repjson = net.sf.json.JSONObject.fromObject(content);

				if ("0000".equals(repjson.getString("respCode"))) {

					log.info("商户：" + merchantNo + "在平安入网成功");

					String respData = repjson.getString("encryptData");

					// 报文AES解密
					String serviceJsonStr = new String(
							AesUtil.decryptAES(TRADE_AES_KEY.getBytes("utf-8"), Base64.decodeBase64(respData)), "utf-8");

					log.info(merchantNo + "平安注册响应响应 解密===respstr:" + serviceJsonStr);

					JSONObject contentJson = JSONObject.fromObject(serviceJsonStr);
					String customerNo = contentJson.getString("customerNo"); // 平台商户编码

					String insertmerchantchannel = "insert into TBL_PAY_MERCHANTCHANNEL (optimistic,channelCode,channelFlag,channelName,createDate,creator,merchantNo,"
							+ "openId,superMerchantNo,superTermid,channelSign,channelDesKey,channelQueryKey,idCard,t0PayResult)"
							+ " values (0,'PINGAN',1,'平安通道',now(),'admin','" + merchantNo + "','','" + customerNo + "','','','','','','99')";

					log.info("商户[{" + merchantNo + "}]入网成功, sql:" + insertmerchantchannel);
					int x = executeSql(insertmerchantchannel, null);

					if (x < 1) {
						log.info("保存数据库失败,程序停止:" + insertmerchantchannel);
						System.exit(1);
					}

					successCount += 1;

					Object[] obj = new Object[] { merchantNo, merchant.get("showName"), "成功" };
					alist.add(obj);

					log.info("将信息保存到TBL_PAY_MERCHANTCHANNEL 中" + insertmerchantchannel + "受影响行数:" + x);

					String upseq = "update sequence_pay_merchantchannel set next_val=next_val+1";
					executeSql(upseq, null);

				} else {

					failCount += 1;

					String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
					int x = executeSql(upmerchantinfoup,
							new Object[] { "PINGAN" + repjson.getString("respMsg"), merchantNo });
					log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);

					log.info("商户：" + merchantNo + "在平安入网失败,原因：" + repjson.getString("respMsg"));

					Object[] obj = new Object[] { merchantNo, merchant.get("showName"), repjson.getString("respMsg") };
					alist.add(obj);

					continue;
				}
			} catch (Exception e) {
				failCount += 1;

				String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
				int x = executeSql(upmerchantinfoup, new Object[] { "PINGAN" + e.getMessage(), merchantNo });
				log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);

				log.info(merchantNo + "商户在平安入网失败:" + e.getMessage());
				continue;
			}
		}
		
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String s = sdf.format(new Date());
			CreateExcle.createExcel2(title, alist, "/opt/sh/data/pingan/", s + "pinganreport");
		} catch (FileNotFoundException e) {
			log.error(new Date() + "报单保存文件失败", e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Long endTime = System.currentTimeMillis();
		log.info("入网完成，共使用" + (endTime - startTime) / 1000 + "秒，成功:" + successCount + "个， 失败：" + failCount + "个");

		log.info("入网结束");
	}

	public static void main(String[] args) throws UnsupportedEncodingException, Exception {

		PingAnMerchantReport merchantreport = new PingAnMerchantReport();
		merchantreport.init(args);

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

	public static Map<Integer, String> mccmap = new HashMap<Integer, String>();

	public static final Integer[] wxMCCType = { 292, 153, 209, 210, 116, 293, 294, 295, 296, 297, 298, 305, 319, 323,
			123, 299, 306, 320, 300, 148, 149, 301, 307, 308, 302, 304, 230, 322, 155, 309 };

	static {
		mccmap.put(292, "2015050700000000");
		mccmap.put(153, "2015050700000000");
		mccmap.put(209, "2015091000052157");
		mccmap.put(210, "2015062600002758");
		mccmap.put(116, "2015062600002758");
		mccmap.put(293, "2015063000020189");
		mccmap.put(294, "2015063000020189");
		mccmap.put(295, "2015063000020189");
		mccmap.put(296, "2015063000020189");
		mccmap.put(297, "2015063000020189");
		mccmap.put(298, "2015063000020189");
		mccmap.put(305, "2015063000020189");
		mccmap.put(319, "2015063000020189");
		mccmap.put(323, "2015063000020189");
		mccmap.put(123, "2015063000020189");
		mccmap.put(299, "2015063000020189");
		mccmap.put(306, "2015063000020189");
		mccmap.put(320, "2015063000020189");
		mccmap.put(300, "2015062600004525");
		mccmap.put(148, "2015062600004525");
		mccmap.put(149, "2015062600004525");
		mccmap.put(301, "2015080600000001");
		mccmap.put(307, "2015080600000001");
		mccmap.put(308, "2015062600004525");
		mccmap.put(302, "2015062600004525");
		mccmap.put(304, "2015062600004525");
		mccmap.put(230, "2016062900190296");
		mccmap.put(322, "2016062900190296");
		mccmap.put(155, "2015063000020189");
		mccmap.put(309, "2015063000020189");
	}
}
