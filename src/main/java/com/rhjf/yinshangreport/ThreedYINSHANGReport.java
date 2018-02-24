package com.rhjf.yinshangreport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.RSAUtil;

import net.sf.json.JSONObject;


public class ThreedYINSHANGReport extends BaseDao{

	// 商户注册 正式地址
	public static final String registerURL = "https://cross-borderpay.bjpos.com:8093/quick-pay-api/v1.0/merchant/regist";

	// 正式机构号
	public static final String organizationID = "00000085";
	
	// 正式私钥
	public static final String prvRAS = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMyPEq+s63kj/6t0h9rOwFZzM4NXlzHqj+yd73ufZPZfsEuJsIQng+rDsv//ptTot+7cY05fIVq4Q2AzSaDKGa01tgvqoJKCojcgYw5rUwehiK5t6rzZl6voamG50uxteUbyTkcbs5C59um8I6/r+hdL7wDYaN8NQ7I4NcQyz6lrAgMBAAECgYEArV1ocdj0rHOVAMO/S/ND0UDLXLpEWrq4BcqIp9YcJC5O5IYqQqaWx8XaE9qgkvs0v8yqoUZAp3lZKNPux8Xg4YXYLFA7mhFxxRR0lpoPqVBZN6dFfUR+g/HfAjffxEnOTGLRk4+3OARIm12IysINFBjtuS+cCtRRrtQQbISTF9kCQQD0PzAqF2+fOhz2beT725HV9ViOHVzlEgUjNay6L3frVCAy+a2tPzeq9PryV36equVkI3vELC+R+0tSZkE4vAd9AkEA1mb6woXHjinFjy2owwXSjoa9Kkr8tp55Rc/rswEIH8a4be9ZzDVAV7ZZyCt/O33Wd3Wr2nddoxyetuAkmiVZBwJATZIb6+JRkxJTzHgOd2a+pGMtYsU0kZPticcsOl2FCnpHV6kwXYtsVKFFad4b2qyP+gPC9QTLfuN+gobzQ9+DyQJABvcHdGRGLE8dFN6l2dgJlAm6gXI4LXKOe/8aKBGDgwzAlwmsgB7GvVK2LfODyZn36p+O+qTgDNl0KnqzlbJa5wJBANtVuvdGV0f2X4wARs/02aVMFnhngYKW26IFS1+QZP495kJbuS61Sz2TqRza+U5eH8phPi5tzIU/4hE+hzCHgzg=";
	
	// 响应状态码  200 代表成功
	public static final String respCode = "200";
	

	Logger log = Logger.getLogger(this.getClass());
	
	
	List<Object[]> list = new ArrayList<Object[]>();
	
	public static void main(String[] args) {
		
	}
	
	
	public int pageCount(){
		
		String sql = "select count(1) as count from tbl_pay_merchant as a INNER JOIN TBL_PAY_MERCHANTBANKCARD as b on a.id=b.ownerId  "
				+ " where a.merchantNo not in (select merchantNo from TBL_PAY_MERCHANTCHANNEL where channelCode='YINSHANG')"
				+ " and agent_id='127' and a.merchantStatus='AVAILABLE' and channelApplyMsg is null ";
		Integer count = Integer.parseInt(queryForMap(sql, new Object[]{"123"}).get("count").toString());
		
		Integer pageCount = count%2000==0?count/2000:count/2000+1;
		
		return pageCount;
	}
	
	
	public void init(){
		int pageCount = pageCount();
		
		CyclicBarrier cyclicBarrier = new CyclicBarrier(pageCount);
		ExecutorService executorService = Executors.newFixedThreadPool(pageCount);
		
		log.info("需要执行线程的个数:" + pageCount); 
		
		for (int i = 0; i < pageCount; i++){
			
			Integer page = i * 2000;
			
			String sql = "select a.* , b.* from tbl_pay_merchant as a INNER JOIN TBL_PAY_MERCHANTBANKCARD as b on a.id=b.ownerId  "
					+ " where a.merchantNo not in (select merchantNo from TBL_PAY_MERCHANTCHANNEL where channelCode='YINSHANG')"
					+ " and agent_id='127' and a.merchantStatus='AVAILABLE' and channelApplyMsg is null limit  " + page +" , 2000";
			
			log.info("i:" + i + "执行的sql ：" + sql);
			
			List<Map<String,Object>> merchantlist = queryForList(sql, null);
			executorService.execute(new ThreedYINSHANGReport().new Task(cyclicBarrier , merchantlist , "--线程:" + i + "  --" ));
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public class Task implements Runnable {

		private CyclicBarrier cyclicBarrier;
		
		private List<Map<String,Object>> merchantlist;
		
		private String name;

		public Task(CyclicBarrier cyclicBarrierm , List<Map<String,Object>> merchantlist , String name) {
			this.cyclicBarrier = cyclicBarrierm;
			this.merchantlist = merchantlist;
			this.name = name;
		}

		public void run() {
			try {
				cyclicBarrier.await();
				
				int successCount = 0;
				int failCount = 0;
				
				
				log.info(name +  "线程开始 , 执行的list长度:" + merchantlist.size());
				
				for (Map<String, Object> map : merchantlist) {
					
					Map<String, Object> params = new TreeMap<String, Object>();
					params.put("pmsBankNo", map.get("bankCode")); // 必填，12位联行号
					params.put("certNo", map.get("legalPersonID"));// 必填，证件号
					params.put("mobile", map.get("linkPhone")); // 必填，结算卡绑定的11位手机号码
					params.put("password", map.get("linkPhone")); // 必填，商户密码
					params.put("cardNo", map.get("accountNo")); // 必填，银行卡号
					params.put("orgId", organizationID); // 必填，6为平台机构号
					params.put("realName", map.get("accountName")); // 必填，结算卡对应的真实姓名
					params.put("account", map.get("linkPhone")); // 必填，11位手机号
					params.put("mchntName", map.get("signName")); // 必填，商户名称
					
					params.put("certType", "00");//  证件类型 非必填，默认00身份证
					params.put("cardType", 1); 	// 结算卡类型   非必填，默认1
					
					
					String feesql = "select * from TBL_ONLINE_PRODUCT_FEE where merchantNo=?";
					List<Map<String,Object>> feelist = queryForList(feesql, new Object[]{map.get("merchantNo")});
					
					log.info(name + "查询商户：" + map.get("merchantNo") + "费率信息"); 
					boolean flag = true;
					if(feelist.size()!=2){
						log.info("商户：" + map.get("merchantNo") + "费率信息种类为：" + feelist.size() + "不符合入网规则");
						list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),"费率信息种类为：" + feelist.size() + "不符合入网规则"});
			            failCount+=1;
						continue;
					}
					
					
					if(!feelist.get(0).get("value").toString().equals(feelist.get(1).get("value").toString()) 
							&& !feelist.get(0).get("t0Fee").toString().equals(feelist.get(1).get("t0Fee"))){
						flag = false;
						log.info(name + "商户:" +  map.get("merchantNo") + "两种支付类型配置不相同, value:" + feelist.get(0).get("value") + " , " + feelist.get(1).get("value") + "; t0Fee :" +
								feelist.get(0).get("t0Fee") + " , " + (feelist.get(1).get("t0Fee"))); 
						list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),"费率配置不一致"});
			            failCount+=1;
						continue;
					}
					
					if(flag){
						params.put("consumFeeType", 0); // 消费手续费类型 必填，0,0 表示费率
						params.put("consumFeeRate", feelist.get(0).get("value")); // 消费手续费费率
						params.put("drawingFeeType", 0); // 提款手续费类型 必填，0表示费率，2表示增值费
						params.put("drawingFeeRate",  feelist.get(0).get("t0Fee")); // 提款手续费费率

						BigDecimal drawingAdd = new BigDecimal(map.get("drawingAdd")==null?"0":map.get("drawingAdd").toString()).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_DOWN); 
						params.put("drawingAdd", drawingAdd);  // 商户提款附加费
						String sign = createSign(params);
						params.put("signature", sign);
						log.info(name + "银商商户注册请求报文" + JSONObject.fromObject(params).toString());

						try {
							String content = HttpClient.post(registerURL, params, null);
							System.out.println(content);
							log.info(name + "银商商户注册响应报文:" + content);
							JSONObject json = JSONObject.fromObject(content);
							if (json.getString("respCode").equals(respCode)) {
								log.info(name + "商户[{" + map.get("merchantNo") + "}]在在银商入网成功了");
								
								Integer seq  = 0;
								synchronized (this) {
								
									String seqSQL = "select * from sequence_pay_merchantchannel";
									seq = Integer.parseInt(queryForMap(seqSQL, null).get("next_val").toString()); 
									log.info(name + "查询序列sql：" + seqSQL + ",查询结果:" + seq); 
									String upseq = "update sequence_pay_merchantchannel set next_val=next_val+1";
									executeSql(upseq,null);
									log.info(name + "更新序列sql:" + upseq); 
								}
								
								
								String insertmerchantchannel = "insert into TBL_PAY_MERCHANTCHANNEL (id,optimistic,channelCode,channelFlag,channelName,createDate,creator,merchantNo,"
										+ "openId,superMerchantNo,superTermid,channelSign,channelDesKey,channelQueryKey,idCard)"
										+ " values (?,?,?,?,?,now(),?,?,?,?,?,?,?,?,?)";
								
								int x = executeSql(insertmerchantchannel, new Object[]{seq,0,"YINSHANG",0,"银商通道","admin",map.get("merchantNo"),"", map.get("linkPhone"),"","","","",map.get("legalPersonID")});
								
								log.info(name + "将信息保存到TBL_PAY_MERCHANTCHANNEL 中" + insertmerchantchannel + "收银上行数:" + x); 
								
							
								successCount+=1;
								
								list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),"成功"});
								
							}else {
								
								String upmerchantinfoup = "update tbl_pay_merchant set channelApplyMsg=? where merchantNo=?";
								int x = executeSql(upmerchantinfoup, new Object[]{"YINSHANG" + json.getString("respMsg"),map.get("merchantNo")});
					            
					            log.info(map.get("merchantNo") + "在银商报单失败了，失败原因是"+json.getString("respMsg"));
					            log.info(name + "更新报件消息：" + upmerchantinfoup + "受影响行数:" + x);
					            
					            list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),json.getString("respMsg")});
					            
					            failCount+=1;
					        }
						} catch (Exception e) {
							failCount+=1;
							list.add(new Object[]{ map.get("merchantNo"),map.get("signName"),e.getMessage()});
							
							log.error(name + map.get("merchantNo") + " 调用银商进件异常--------------", e);
						}
					}
				}
				try {
					YINSHANGReport yinshang = new YINSHANGReport();
		            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		            String s = sdf.format(new Date());
		            yinshang.createExcel2(list, s);
		        } catch (FileNotFoundException e) {
		            log.error(new Date()  + "报单保存文件失败", e);
		            e.printStackTrace();
		        } catch (IOException e) {
					e.printStackTrace();
				}
				log.info(name +  "入网完成，共：" +merchantlist.size()+ "个, 成功:" + successCount + "个， 失败：" + failCount + "个"); 
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
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
}
