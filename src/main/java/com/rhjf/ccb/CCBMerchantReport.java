package com.rhjf.ccb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.rhjf.base.BaseDao;
import com.rhjf.utils.AESUtil;
import com.rhjf.utils.CreateExcle;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.RandomUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 *	  建设银商 商户入网
 *  
 * @author a
 *
 */
public class CCBMerchantReport extends BaseDao{

	
	/** 测试注册地址 **/
	public static final String registURL = "https://api.jia007.com/api-center/rest/v1.0/yqt/registerMerchant";
	
	/** 注册测试商编 **/
	public static final String regisMerchatNo = "1051100010000722";
	
	public static final String regisKey = "0bd9016221fa4d3db7fd5b9b9410c51c8709212f605947a08054ee9443ea25cb";
	
	public void init(String number){
		
		Long startTime = System.currentTimeMillis();
		
		log.info("开始执行建行入网操作");
		
		List<Object[]> list = new ArrayList<Object[]>();
		String[] title = {"商户号" , "商户名称" , "入网结果"};
		
//		String merchantlistSQL = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b on a.id=b.ownerId"
//				+ " INNER JOIN (select merchantNo from tbl_online_order where date(createDate)=date(DATE_ADD(now(),INTERVAL -1 day)) and "
//				+ " merchantNo not in (select merchantNo from tbl_pay_merchantchannel where channelCode='CCB') GROUP BY merchantNo) as c"
//				+ " where a.merchantStatus='AVAILABLE' and (a.channelApplyMsg not like 'CCB%' or a.channelApplyMsg is null ) and a.merchantNo=c.merchantNo and agent_id='175' ";
//		
		String merchantlistSQL = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b on a.id=b.ownerId where  "
				+ "a.merchantStatus='AVAILABLE' and (a.channelApplyMsg not like 'CCB%' or a.channelApplyMsg is null ) and a.merchantNo='" + number + "'";
		
		log.info("查询没有在建行入网的商户 : sql ==== " + merchantlistSQL);
		
		List<Map<String,Object>> merchantlist = queryForList(merchantlistSQL , null);
	
		log.info("查询商户条数：" + merchantlist.size()); 
		
		int successCount = 0;
		int failCount = 0;
		
		for (Map<String, Object> merchantMap : merchantlist) {
			
			String merchantNo =  merchantMap.get("merchantNo").toString();
			
			log.info("商户号：" + merchantNo + "开始向建行入网");
			
			Map<String, Object> registmap = new HashMap<String,Object>();
			registmap.put("requestNo",  RandomUtils.getRandomString(7));
			registmap.put("agentNo", regisMerchatNo);		//代理商编号
			registmap.put("province", merchantMap.get("province"));							//商户所在省份
			registmap.put("city", merchantMap.get("city"));
			registmap.put("address", merchantMap.get("address"));
			registmap.put("businessLicence", merchantMap.get("businessLicense"));
			
			if("PERSON".equals(merchantMap.get("merchantType").toString())){
				registmap.put("signedName", merchantMap.get("linkman"));
			}else{
				failCount+=1;
				
				String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
				int x = executeSql(upmerchantinfoup, new Object[]{"CCB非个人商户，停止入网" ,merchantNo});
				log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
				
				
				log.info("商户：" + merchantNo + "在建行入网失败,原因：非个人商户，停止入网" );
				Object[] obj = new Object[]{merchantNo, merchantMap.get("showName") , "非个人商户，停止入网"};
				list.add(obj);
				
				continue;
			}
			
			registmap.put("customerStyle" , "PERSON");
			
			registmap.put("idCard", merchantMap.get("legalPersonID"));
			registmap.put("legalPerson", merchantMap.get("legalPerson"));
			registmap.put("contactor", merchantMap.get("linkman"));
			registmap.put("bindMobile", merchantMap.get("linkPhone"));
			registmap.put("bankAccountNumber", merchantMap.get("accountNo"));
			
			if("TOPRIVATE".equals(merchantMap.get("settleBankType").toString())){
				registmap.put("bankAccountType", "PRIVATE_CASH");
			}else{
				registmap.put("bankAccountType", "PUBLIC_CASH");
			}
			
			registmap.put("bankBranchName", merchantMap.get("bankBranch"));
			registmap.put("bankBranchCode", merchantMap.get("bankCode"));
			registmap.put("accountName", merchantMap.get("accountName"));
			registmap.put("bankCardProvince", merchantMap.get("bankProv"));
			registmap.put("bankCardCity", merchantMap.get("bankCity"));
			registmap.put("minSettleAmount", "0");
			registmap.put("merchantShortName", merchantMap.get("showName"));
			registmap.put("servicePhone", merchantMap.get("linkPhone"));
			
			log.info("查询商户" + merchantNo + "微信费率");
			
			String wxfeeSQL = "select * from tbl_online_product_fee where (bankId='WX' or bankId='Alipay') and merchantNo=?";
			
			List<Map<String,Object>> feeMaplist = queryForList(wxfeeSQL, new Object[]{merchantNo});
			
			
			if(feeMaplist==null||feeMaplist.size()==0){
				failCount+=1;
				
				String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
				int x = executeSql(upmerchantinfoup, new Object[]{"CCB微信费率为空，不执行入网操作" ,merchantNo});
				log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
				
				log.info("查询商户" + merchantNo + "微信费率为空，不执行入网操作");
				continue;
			}
			
			
			
			JSONArray payArray = new JSONArray();

			
			for (int i = 0; i < feeMaplist.size(); i++) {
				Map<String,Object> feeMap = feeMaplist.get(i);
				
				JSONObject payJSON = null;
				
				if("WX".equals(feeMap.get("bankId").toString())){
					
					payJSON = new JSONObject();
					
					int wxlength = CCBMerchantUpdate.WXbusiness.length;
					Random random = new Random(wxlength-1);
					int index = random.nextInt(wxlength-1);
					String cccNumber = CCBMerchantUpdate.WXbusiness[index];
					// ALIPAY_SCAN
					payJSON.put("payTool", "WECHAT_SCAN"); //N
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					
					payJSON = new JSONObject();
					
					payJSON.put("payTool", "WECHAT_PUBLIC"); //N
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					
					payJSON = new JSONObject();
					
					payJSON.put("payTool", "WECHAT_MICROPAY"); //N
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					
					
				}else{
					payJSON = new JSONObject();
					
					int wxlength = CCBMerchantUpdate.WXbusiness.length;
					Random random = new Random(wxlength-1);
					int index = random.nextInt(wxlength-1);
					String cccNumber = CCBMerchantUpdate.WXbusiness[index];
					
					// ALIPAY_SCAN
					payJSON.put("payTool", "ALIPAY_SCAN");
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					payJSON = new JSONObject();
					payJSON.put("payTool", "ALIPAY_PUBLIC");
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					
					payJSON = new JSONObject();
					payJSON.put("payTool", "ALIPAY_MICROPAY");
					payJSON.put("feeRate", feeMap.get("value"));
					payJSON.put("business", cccNumber);
					payArray.add(payJSON);
					
					
				}
				registmap.put("payProduct", payArray.toString());
			}
			
			
			
			Map<String,Object> params = new HashMap<String,Object>();
			
			log.info("商户入网：" + merchantNo + " ====请求报文 原文：" + registmap.toString()); 
			
			String data = AESUtil.encrypt(JSON.toJSONString(registmap), regisKey.substring(0, 16)); 
			params.put("data", data);
			params.put("appKey", regisMerchatNo);
			
			
			log.info("商户入网：" + merchantNo + " ===请求报文 密文：" + params.toString()); 
			
			String content = null;
			try {
				content = HttpClient.post(registURL, params, "1");
				
				log.info("商户入网：" + merchantNo + " 建行注册响应响应:" + content);
				
				String respstr = AESUtil.decrypt(content,  regisKey.substring(0, 16));
				
				log.info("商户入网：" + merchantNo + " 建行注册响应响应 解密===respstr:" + respstr);
				
				net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(respstr);
				if ("1".equals(json.getString("code"))) {
					
					log.info("商户：" + merchantNo + " 在建行入网成功");
					
					successCount +=1;
					
					String superMerchantNo =  json.getString("merchantNo");
					
					String insertmerchantchannel = "insert into TBL_PAY_MERCHANTCHANNEL (optimistic,channelCode,channelFlag,channelName,createDate,creator,merchantNo,"
							+ "openId,superMerchantNo,superTermid,channelSign,channelDesKey,channelQueryKey,idCard)"
							+ " values (0,'CCB',0,'建行通道',now(),'admin','"+merchantNo+"','','"+superMerchantNo+"','','','','','" + merchantMap.get("legalPersonID")+"')";
					executeSql(insertmerchantchannel, null);
					
					
					Object[] obj = new Object[]{merchantNo, merchantMap.get("showName") , "成功"};
					list.add(obj);
					
					String upseq = "update sequence_pay_merchantchannel set next_val=next_val+1";
					executeSql(upseq,null);
					
					log.info("更新序列sql:" + upseq); 
					
				} else { 
					failCount+=1;
					
					String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
					int x = executeSql(upmerchantinfoup, new Object[]{"CCB" + json.getString("message")  ,merchantNo});
					log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
					
					log.info("商户：" + merchantNo + " 在建行入网失败,原因：" + json.getString("message"));
					Object[] obj = new Object[]{merchantNo, merchantMap.get("showName") , json.getString("message")};
					list.add(obj);
				}
				
			} catch (Exception e) {
				failCount+=1;
				
				String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
				int x = executeSql(upmerchantinfoup, new Object[]{"CCB" + e.getMessage()  ,merchantNo});
				log.info("商户号： " + merchantNo + " 更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
				
				log.info("商户：" + merchantNo + " 在建行入网失败,原因：" + e.getMessage());
				Object[] obj = new Object[]{merchantNo, merchantMap.get("showName") , e.getMessage()};
				list.add(obj);
			}
		}
		try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String s = sdf.format(new Date());
            CreateExcle.createExcel2( title  , list, "/opt/sh/data/ccb/" ,  s + "ccbreport");
        } catch (FileNotFoundException e) {
            log.error(new Date()  + "报单保存文件失败", e);
            e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
		
		Long endTime = System.currentTimeMillis();
		log.info("入网完成，共使用" + (endTime-startTime)/1000 + "秒，成功:" + successCount + "个， 失败：" + failCount + "个"); 
	}
	
	public static void main(String[] args) {
		
		String number = "B100197743";
		
		CCBMerchantReport ccb = new CCBMerchantReport();
		ccb.init(number);
	}
}
