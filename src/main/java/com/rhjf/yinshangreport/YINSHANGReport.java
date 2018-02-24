package com.rhjf.yinshangreport;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.RSAUtil;

import net.sf.json.JSONObject;

/**
 *  银商入网 定时任务
 * @author a
 *
 */
public class YINSHANGReport extends BaseDao{
	
	// 商户注册 测试地址
//	public static final String registerURL = "http://122.112.29.24:18080/quick-pay-api/v1.0/merchant/regist"; 
//	public static final String registerURL = "http://122.112.29.24:18080/quick-pay-api/v1.0/merchant/registPublic";
	// 商户注册 正式地址
//	public static final String registerURL = "https://cross-borderpay.bjpos.com:8093/quick-pay-api/v1.0/merchant/regist";
	public static final String registerURL = "https://cross-borderpay.bjpos.com:8093/quick-pay-api/v1.0/merchant/registPublic";

	// 测试机构号
//	public static final String organizationID = "00000070";
	// 正式机构号
	public static final String organizationID = "00000085";
	
	// 测试私钥
//	public static final String prvRAS = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAINjMj4qNkKid/MpL/wFcepscKp1jcMhQ4ti07wvKSkHIbklPHK/uo2WH0hrJBfX6JtMrCvBHtRqaj1mJj9NVsglzblbp9GhWME2DkN6xWA01k0y7vjODAtvA/YooBBiK1+2CnDdbpmIi6wdDbkP0PvwcMJWYwhgPDh58RA+7fttAgMBAAECgYBJGTFk4Ltbi2TNBodJ4gWk9TwhulFq1ODtdchzlJGD4BHlHlBpTz6Nc45oDiQAmAE0Fg5cMY/jgmklS+XPpkm3OPUKeJ22FMaJGoyVIk981UVfx2wGidAmbpK/Jwxm0OEsYZADoF0dhn4mfHIgpF1d4DvCsqrV7ZsDt113xrqwiQJBAMAuf+8V5j3LNzRkIFEozjurmViqVQcZSxTytEp+w4SrIV9PNjq5PsYIXEmJzvLJPBf76WEQIeIb6kcnRVYzcb8CQQCvBIqeERtv7wFtHcaMluOi574q+6ZOS15q8Akq33Gz1p8DThJ1bocCS0b9V5cbirCDs413JUJ2/HmZf05FUYXTAkBTrqvEdru34poaNRMhY+xRbUorope9rJTV/UzmN0Z5qW6xqrNJZMphvtg4qUo0y81gADBCNJ0ccN1VuFLn0yTPAkAs0Q7l9x99hEhrNqq3KZRVDN7HhvVJK0ecPqc6UUl+ccD6Sa20YH81+SzOhvVs2hDlSL86+VGRwoko406ZtYx9AkAb+xHGZwr+9S72vuM7+u5iR1TXuLRayrolyz1xV1kiyCAIk2niKYYvAQM5TAws3m9cJt8s6zoCetLogPRSsIHQ";	
//	public static final String prvRAS = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMM9pEqpiFK964PR4LD4YEPkr/hAWMNvjSpGyU4NTQiFT8iHkJDSr9iJj2QQxF6w7+/srpCCe8OBHX4pZubDtpyv0fvkpWC2B2daJeOfSRvIuGDjTldXqNFPN6UrrpxU0IOvbmpqrSjqWCtZeUgNhGRttsN1sAYvVh9KPUzewmZpAgMBAAECgYB4lbDMAEtXNz+nyc+p3a2vISZiAHQSOOqKKvAYCfUDBztQkhICsG21IrjyR7zQ8x0uJRNn439HL46kpjOM4WH97NTdW8YIAavpgbKhslSBazl5PEZPoVlpN2mIgzYtSdUpa3Syl+9031TdoKMl8gQN3YlGTgj/gZjV6f/SXpKS9QJBAPYjX8VgHtLMKpFTitv4Kru7FCZphOlsS6ik8TcFLuiA7I+svH/b37u2dymR8TEknsZ43qluRspicMtdFsk3nWsCQQDLEDdEMOMltaYHJkcZQNc/h6QExope2TQgwlpRrevJwpHQVowIUKbGTZS03eFf2XbND9bTLinO1fjKnuOAakx7AkEA0y0eahVB3NHWY8EtjfSplU+4xgwaQLrtXs/FNNN5n3mdfNdTEs/ucPrn2f6g1Oz6XIYvY2Z9zf4PkCtZ9WGF6QJAULlYjxY0JniI0QzJdOOO7iV48aEPvtbv3xoEF7ZhJqrflofhUSjms6yBskkGYDkt/iUOzJLscdoj9kClxhX74wJAFfgjUB7x8ajyvJHjEdiQWNXK1GjZr+5jgw5AyhbR5mAMLz6o0qGAMe4SXrsz2B9oqhdSvL8KgPu6nJ6rQS+12A==";
	// 正式私钥
	public static final String prvRAS = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMyPEq+s63kj/6t0h9rOwFZzM4NXlzHqj+yd73ufZPZfsEuJsIQng+rDsv//ptTot+7cY05fIVq4Q2AzSaDKGa01tgvqoJKCojcgYw5rUwehiK5t6rzZl6voamG50uxteUbyTkcbs5C59um8I6/r+hdL7wDYaN8NQ7I4NcQyz6lrAgMBAAECgYEArV1ocdj0rHOVAMO/S/ND0UDLXLpEWrq4BcqIp9YcJC5O5IYqQqaWx8XaE9qgkvs0v8yqoUZAp3lZKNPux8Xg4YXYLFA7mhFxxRR0lpoPqVBZN6dFfUR+g/HfAjffxEnOTGLRk4+3OARIm12IysINFBjtuS+cCtRRrtQQbISTF9kCQQD0PzAqF2+fOhz2beT725HV9ViOHVzlEgUjNay6L3frVCAy+a2tPzeq9PryV36equVkI3vELC+R+0tSZkE4vAd9AkEA1mb6woXHjinFjy2owwXSjoa9Kkr8tp55Rc/rswEIH8a4be9ZzDVAV7ZZyCt/O33Wd3Wr2nddoxyetuAkmiVZBwJATZIb6+JRkxJTzHgOd2a+pGMtYsU0kZPticcsOl2FCnpHV6kwXYtsVKFFad4b2qyP+gPC9QTLfuN+gobzQ9+DyQJABvcHdGRGLE8dFN6l2dgJlAm6gXI4LXKOe/8aKBGDgwzAlwmsgB7GvVK2LfODyZn36p+O+qTgDNl0KnqzlbJa5wJBANtVuvdGV0f2X4wARs/02aVMFnhngYKW26IFS1+QZP495kJbuS61Sz2TqRza+U5eH8phPi5tzIU/4hE+hzCHgzg=";
	
	// 机构公钥
//	public static final String pubRAS = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnhLgHg4waGKfVgAINZNfGaMqKuQCPl32mt2/uY2NMShyoSZGOojpPCbtTft4lxXmyHcEQbm10uMpNbzl6QzFCuOoJwkQZ68G5pHjZ7jg9pvOPyeJKljrK7+2vQGL/2nZdZMuyNEfKaCmz265NWDcLA67fb8MGWItFMXOY29d7mwIDAQAB";
	
	// 平台公钥
//	public static final String platformPubRAS = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxzVuaVK+Xh4LmS+qVPppK7cbKBuQUbmWcbjSYKWxf/wkmP3umNQE6neu4Lswy1JBh0Joo/piYkeTialZc/VYWaXSUbFMAaoAHf0zWYXpBeU6H6VKxlJVCqyyhrM3j6hySnRp2BeaRKsZtE73MwnAOCbx3sWPkWTgfrXkM7ShLbVaBHGmmrHzzZuibaERajGisCBK3o1yMW6j8nu84KKISygF6ZBDCaSzIoA0W4PjwnpxXWbt4plu7YIu3tFikzuCQHd28FRqoEtS5ht+MiEjJhFwUZwDeCCjFiEaFgowjqbh5q+f9CfhFbVlble1qYMoKjysRcUhK0LpwLztXUooRQIDAQAB";
	
	// 响应状态码  200 代表成功
	public static final String respCode = "200";
	

	Logger log = Logger.getLogger(this.getClass());
	
	List<Map<String,Object>> merchantlist = null;
	
	public void init(){
		
		Long startTime = System.currentTimeMillis();
		
		int successCount = 0;
		int failCount = 0;
		
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		log.info("开始银商入网操作 "); 
//		String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN TBL_PAY_MERCHANTBANKCARD as b on a.id=b.ownerId "
//				+ "where a.merchantNo not in (select merchantNo from TBL_PAY_MERCHANTCHANNEL where channelCode='YINSHANG' and merchantStatus='AVAILABLE')  and a.id='1376'"
//				+ " limit 10";
		
//		String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN TBL_PAY_MERCHANTBANKCARD as b on a.id=b.ownerId  "
//				+ " where a.merchantNo not in (select merchantNo from TBL_PAY_MERCHANTCHANNEL where channelCode='YINSHANG')"
//				+ " and agent_id=? and a.merchantStatus='AVAILABLE' and channelApplyMsg is null ";
		
		String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN TBL_PAY_MERCHANTBANKCARD as b on a.id=b.ownerId  "
				+ " where a.merchantNo not in (select merchantNo from TBL_PAY_MERCHANTCHANNEL where channelCode='YINSHANG') "
				+ " and a.merchantStatus='AVAILABLE' and  a.channelApplyMsg not LIKE 'YINSHANG%'";
		
		
		log.info("查询没有在银商报件过的商户，查询记录为1000条一次，执行sql：" + sql );
		merchantlist = queryForList(sql, null);
		log.info("返回的条数：" +  merchantlist.size()); 
		
		for (Map<String, Object> map : merchantlist) {
			
			
			String feesql = "select * from TBL_ONLINE_PRODUCT_FEE where merchantNo=?";
			List<Map<String,Object>> feelist = queryForList(feesql, new Object[]{map.get("merchantNo")});
			log.info("查询商户：" + map.get("merchantNo") + "费率信息"); 
			boolean flag = true;
			if(feelist.size()!=2){
				flag = false;
				log.info("商户：" + map.get("merchantNo") + "费率信息种类为：" + feelist.size() + "不符合入网规则");
				list.add(new Object[]{ map.get("merchantNo"),"" ,map.get("signName"), "" ,"费率信息种类为：" + feelist.size() + "不符合入网规则"});
	            failCount+=1;
				continue;
			}
			
			String SEQ = "select * from seq";
			Map<String, Object> seqMap = queryForMap(SEQ, null);
			Integer number = Integer.parseInt(seqMap.get("nextVal").toString());
			Integer alipay = Integer.parseInt(seqMap.get("alipay").toString());
			
			for (int j = 0 ; j < feelist.size() ; j++) {
				
				Map<String, Object> map2  = feelist.get(j);
				
				for (int i = 0; i < 2; i++) {
					Map<String, Object> params = new TreeMap<String, Object>();
					
					String accountNumber = String.format("%010d", number); 
					String alipayNumber = String.format("%010d", alipay);
					
					String accuntno = "";
					
					if(map2.get("bankId").equals("WX")){
						log.info(map.get("merchantNo") +  "上报微信信息");
						accuntno =  "8" + accountNumber ;
						params.put("account", accuntno); // 必填，11位手机号
						number += 1;
					}else{
						log.info(map.get("merchantNo") +  "上报支付宝信息"); 
						accuntno =  "9" + alipayNumber ;
						params.put("account", accuntno); // 必填，11位手机号
						alipay += 1;
					}
					
					
					
					params.put("pmsBankNo", map.get("bankCode")); // 必填，12位联行号
					params.put("certNo", map.get("legalPersonID"));// 必填，证件号
					params.put("mobile", map.get("linkPhone")); // 必填，结算卡绑定的11位手机号码
					params.put("password", map.get("linkPhone")); // 必填，商户密码
					params.put("cardNo", map.get("accountNo")); // 必填，银行卡号
					params.put("orgId", organizationID); // 必填，6为平台机构号
					params.put("realName", map.get("accountName")); // 必填，结算卡对应的真实姓名
					
					params.put("mchntName", map.get("showName")); // 必填，商户名称
					
					params.put("certType", "00");//  证件类型 非必填，默认00身份证
					params.put("cardType", 1); 	// 结算卡类型   非必填，默认1
					
					
//					if(!feelist.get(0).get("value").toString().equals(feelist.get(1).get("value").toString()) 
//							&& !feelist.get(0).get("t0Fee").toString().equals(feelist.get(1).get("t0Fee"))){
//						flag = false;
//						log.info("商户:" +  map.get("merchantNo") + "两种支付类型配置不相同, value:" + feelist.get(0).get("value") + " , " + feelist.get(1).get("value") + "; t0Fee :" +
//								feelist.get(0).get("t0Fee") + " , " + (feelist.get(1).get("t0Fee"))); 
//						list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),"费率配置不一致"});
//			            failCount+=1;
//						continue;
//					}
					
					if(flag){
//						params.put("consumFeeType", 0); // 消费手续费类型 必填，0,0 表示费率
//						params.put("consumFeeRate", feelist.get(0).get("value")); // 消费手续费费率
//						params.put("drawingFeeType", 0); // 提款手续费类型 必填，0表示费率，2表示增值费
//						params.put("drawingFeeRate",  feelist.get(0).get("t0Fee")); // 提款手续费费率
						
						params.put("consumFeeType", 0); // 消费手续费类型 必填，0,0 表示费率
						
						
						String fee = "";
						if(i==0){
							log.info(map.get("merchantNo") +   "上报T0");
							fee =  map2.get("t0Fee").toString();
							params.put("consumFeeRate", map2.get("t0Fee")); // 消费手续费费率
						}else if(i==1){
							log.info(map.get("merchantNo") +  "上报T1");
							fee =  map2.get("value").toString();
							params.put("consumFeeRate", map2.get("value")); // 消费手续费费率
						}
						
						Integer t0PayResult = 0;
						
						
						if(map2.get("bankId").equals("WX")){
							if(i==0){
								t0PayResult = 0;
							}else if(i==1){
								t0PayResult = 1;
							}
						}else{
							if(i==0){
								t0PayResult = 3;
							}else if(i==1){
								t0PayResult = 4;
							}
						}
						
						
						params.put("drawingFeeType", 2); // 提款手续费类型 必填，0表示费率，2表示增值费
						params.put("drawingFee",  "0"); // 提款手续费费率
						
						params.put("drawingAdd", "0.0");  // 商户提款附加费
						String sign = createSign(params);
						params.put("signature", sign);
						log.info("银商商户注册请求报文" + JSONObject.fromObject(params).toString());

						String content = "";
						JSONObject json =  null;
						try {
							content = HttpClient.post(registerURL, params, null);

							log.info("银商商户注册响应报文:" + content);
							json = JSONObject.fromObject(content);
							if (json.getString("respCode").equals(respCode)) {
								log.info("商户[{" + map.get("merchantNo") + "}]在在银商入网成功了");
								
//								String seqSQL = "select * from sequence_pay_merchantchannel";
//								Integer seq = Integer.parseInt(queryForMap(seqSQL, null).get("next_val").toString()); 
								
//								log.info("查询序列sql：" + seqSQL + ",查询结果:" + seq); 
								
								String insertmerchantchannel = "insert into TBL_PAY_MERCHANTCHANNEL (optimistic,channelCode,channelFlag,channelName,createDate,creator,merchantNo,"
										+ "openId,superMerchantNo,superTermid,channelSign,channelDesKey,channelQueryKey,idCard,t0PayResult)"
										+ " values (0,'YINSHANG',0,'银商通道',now(),'admin','"+map.get("merchantNo")+"','','"+accuntno+"','','','','','"+map.get("legalPersonID")+"','"+t0PayResult+"')";
								
								// new Object[]{0,"YINSHANG",0,"银商通道","admin",map.get("merchantNo"),"",accuntno,"","","","",map.get("legalPersonID"),t0PayResult}
								
								// ,sql数据:" + "(0,YINSHANG,0,银商通道,admin," + map.get("merchantNo") + ",''," + accuntno + ",'','','',''," +map.get("legalPersonID") + "," +t0PayResult + ")
								
								log.info("商户[{" + map.get("merchantNo") + "}]入网成功, sql:" + insertmerchantchannel);
								int x = executeSql(insertmerchantchannel, null);
							
								if(x < 1 ){
									log.info("保存数据库失败,程序停止:" + insertmerchantchannel); 
									System.exit(1);
								}
								
								
								log.info("将信息保存到TBL_PAY_MERCHANTCHANNEL 中" + insertmerchantchannel + "收银上行数:" + x); 
								
								String upseq = "update sequence_pay_merchantchannel set next_val=next_val+1";
								executeSql(upseq,null);
								
								log.info("更新序列sql:" + upseq); 
								
								successCount+=1;
								
								list.add(new Object[]{ map.get("merchantNo"), accuntno ,map.get("signName"),fee,"成功"});
								
							}else {
								
								String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
								int x = executeSql(upmerchantinfoup, new Object[]{"YINSHANG" + json.getString("respMsg"),map.get("merchantNo")});
					            
					            log.info(map.get("merchantNo") + "在银商报单失败了，失败原因是"+json.getString("respMsg"));
					            log.info("更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
					            
					            list.add(new Object[]{ map.get("merchantNo"), accuntno ,map.get("signName"),fee ,json.getString("respMsg")});
					            
					            failCount+=1;
					        }
						} catch (Exception e) {
							failCount+=1;
							list.add(new Object[]{ map.get("merchantNo"), accuntno ,map.get("signName"),fee,e.getMessage()});
							
							String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
							int x = executeSql(upmerchantinfoup, new Object[]{"YINSHANG" + json.getString("respCode"),map.get("merchantNo")});
							log.info("更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
							
							log.error(map.get("merchantNo") + " , 上报账号：" + accuntno + " ， 请求报文：" + JSONObject.fromObject(params).toString() + " , 响应报文:" + json.toString()+ " 调用银商进件异常--------------", e);
						}
					}
				}
			}
			String updateseq = "update seq set nextVal=? , alipay = ?";
			executeSql(updateseq, new Object[]{number , alipay});
		}
		
		try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String s = sdf.format(new Date());
            createExcel2(list, s);
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
		YINSHANGReport yinshang = new YINSHANGReport();
		yinshang.init();
	}
	
	/**
	 *     根据请求参数 制作签名
	 * @param url
	 * @param resultMap
	 * @return   请求接口返回的数据
	 */
	public static String createSign(Map<String,Object> resultMap ){
		StringBuffer sbf = new StringBuffer();
		
		Map<String , Object> map = resultMap;
		
		for (String key : map.keySet()) {
			sbf.append(key);
			sbf.append("=");
			sbf.append(map.get(key));
			sbf.append("&");
		}
		String string1 = "";
		try {
			string1 = sbf.substring(0,sbf.toString().length()-1);
			return RSAUtil.sign(string1.getBytes() , prvRAS);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	

	public void createExcel2(List<Object[]> list , String date) throws IOException{
		if(!new File("/opt/sh/data/yinshang/").exists()){
			new File("/opt/sh/data/yinshang/").mkdirs();
		}
		log.info("银商入网 : " + list.size() + "开始创建Excel文件");
		
		   // 创建Excel的工作书册 Workbook,对应到一个excel文档  
	    HSSFWorkbook wb = new HSSFWorkbook();  
	  
	    // 创建Excel的工作sheet,对应到一个excel文档的tab  
	    HSSFSheet sheet = wb.createSheet("银商入网数据");  
	  
	    // 生成一个样式  
        HSSFCellStyle style = wb.createCellStyle();  
        // 设置这些样式  
        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);  
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);  
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        // 生成一个字体  
        HSSFFont font = wb.createFont();  
        font.setColor(HSSFColor.VIOLET.index);  
        font.setFontHeightInPoints((short) 12);  
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
        // 把字体应用到当前的样式  
        style.setFont(font);  
        // 生成并设置另一个样式  
        HSSFCellStyle style2 = wb.createCellStyle();  
        style2.setFillForegroundColor(HSSFColor.WHITE.index);  
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);  
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 生成另一个字体  
        HSSFFont font2 = wb.createFont();  
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);  
        // 把字体应用到当前的样式  
        style2.setFont(font2);  
        HSSFFont font3 = wb.createFont();
        font3.setColor(HSSFColor.BLACK.index);
	  
        
	    // 创建Excel的sheet的一行  
	    HSSFRow row = sheet.createRow(0);  
	    
	    HSSFCell cell = row.createCell(0);  
	    
	    // "商户编号", "商户名称", "报单结果"
	    cell.setCellValue("商户编号");
	    cell = row.createCell(1);
	    cell.setCellValue("上送编号");
	    cell = row.createCell(2);
	    cell.setCellValue("商户名称");
	    cell = row.createCell(3);
	    cell.setCellValue("费率");
	    cell = row.createCell(4);
	    cell.setCellValue("报单结果");
	    
	    
	    for(int i=0 ; i< list.size() ; i++ ){
	    	
			row = sheet.createRow(i + 1);
			// 设置单元格的样式格式
			cell = row.createCell(0);
			cell.setCellValue(list.get(i)[0].toString());

			cell = row.createCell(1);
			cell.setCellValue(list.get(i)[1].toString());

			cell = row.createCell(2);
			cell.setCellValue(list.get(i)[2]==null?"":list.get(i)[2].toString());
			
			cell = row.createCell(3);
			cell.setCellValue(list.get(i)[3]==null?"":list.get(i)[3].toString());
			
			cell = row.createCell(4);
			cell.setCellValue(list.get(i)[4]==null?"":list.get(i)[4].toString());
	    }
	    FileOutputStream os = new FileOutputStream("/opt/sh/data/yinshang/" + date +"yinshangreport.xls");  
	    wb.write(os);  
	    os.close();  
	    wb.close();
	    log.info("生成文件完成"); 
	}
}
