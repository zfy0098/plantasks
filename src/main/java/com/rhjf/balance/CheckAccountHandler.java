package com.rhjf.balance;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.UtilKey;

public class CheckAccountHandler extends BaseDao{

	public final static String orgStr = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public final static String url = "https://download.swiftpass.cn/gateway";
	
	public final static String channelName = "XINGYE";
	
	public final static Integer listSize = 1000;
	
	public String getRandomStr(int length) {
		StringBuffer sb = new StringBuffer();
		int n;
		for (int i = 0; i < length; i++) {
			n = (int) (Math.random() * 62);
			sb.append(orgStr.substring(n, n + 1));
		}
		return sb.toString();
	}
	
	public SortedMap<Object,Object> createCheckAccountMessageForSign(String date,String superMerchantNo){
		SortedMap<Object,Object> params1 = new TreeMap<Object,Object>();
		//获得指定长度的随机字符串
		String nonce_str = getRandomStr(15);
		params1.put("service", "pay.bill.merchant");
		params1.put("version","1.0");
		params1.put("bill_date",date);
		params1.put("bill_type","ALL");
		params1.put("mch_id",superMerchantNo);
		params1.put("nonce_str",nonce_str);

		return params1;
	}
	
    public String createSign(String characterEncoding,SortedMap<Object,Object> parameters,String key){  
        StringBuffer sb = new StringBuffer();  
        Set<?> es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）  
        Iterator<?> it = es.iterator();  
        while(it.hasNext()) {  
            @SuppressWarnings("unchecked")
			Map.Entry<String,Object> entry = (Map.Entry<String,Object>)it.next();  
            String k = entry.getKey();  
            Object v = entry.getValue();  
            if(null != v && !"".equals(v)   
                    && !"sign".equals(k) && !"key".equals(k)) {  
                sb.append(k + "=" + v + "&");  
            }  
        }  
        sb.append("key=" + key);  
        return UtilKey.MD5(sb.toString()).toUpperCase();
    }
    
	public  String createCheckAccountMessage(String date,String nonce,String sign,String superMerchantNo){

		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sBuilder.append("<xml>");
		sBuilder.append("<service><![CDATA[pay.bill.merchant]]></service>");
		sBuilder.append("<version><![CDATA["+ "1.0" +"]]></version>");
		sBuilder.append("<bill_date><![CDATA["+ date +"]]></bill_date>");
		sBuilder.append("<bill_type><![CDATA[ALL]]></bill_type>");
		sBuilder.append("<mch_id><![CDATA["+ superMerchantNo +"]]></mch_id>");
		sBuilder.append("<nonce_str><![CDATA["+ nonce +"]]></nonce_str>");
		sBuilder.append("<sign><![CDATA["+ sign +"]]></sign>");
		sBuilder.append("</xml>");
		String plainXML = sBuilder.toString();
		return plainXML;
	}
	
	
	public List<Map<String,Object>> dealCheckAccountMessage(String payrequest_result,String channelName){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String[] titleAndValue = payrequest_result.split("[,]{0,1}`");
		int titlength = titleAndValue[0].split(",").length;
		int valueIndex = 0;
		Map<String,Object> s = null;

		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < titleAndValue.length; i++) {
			if (i != 0 && i != titleAndValue.length - 6) {
				switch ((valueIndex) % titlength) {
				case 0:
					s = new HashMap<String,Object>();
					valueIndex = 0;
					break;
				case 4:
					s.put("superMerchantNo" , titleAndValue[i].trim());
					break;
				case 8:
					s.put("bankOrderId",titleAndValue[i].trim());
					try {
						if(titleAndValue[i].length()>28){
							sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							Date date = sdf.parse("20"+titleAndValue[i].substring(14,26).trim());
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
							s.put("trxDate", sdf.format(date));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case 14:
					s.put("trxAmount" , new BigDecimal(titleAndValue[i].trim()).setScale(2,BigDecimal.ROUND_HALF_UP));
					break;
				case 24:
					s.put ("trxFee" , new BigDecimal(titleAndValue[i].trim()).setScale(2,BigDecimal.ROUND_HALF_UP));
					s.put ("channelName" , channelName);
					list.add(s);
					break;
				default:
					break;
				}
				valueIndex++;
			}
		}
		return list;
	}
    
	public void  downloadXINGYEcheckAccount(){
		
		log.info("开始下载兴业银行对账文件");
		
		int count = 0;
		BigDecimal sumAmount = new BigDecimal("0").setScale(2, BigDecimal.ROUND_HALF_UP);
	    BigDecimal sumFee = new BigDecimal("0").setScale(2, BigDecimal.ROUND_HALF_UP);
	    String date1 = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		
		String date = sdf.format(c.getTime());
		
		c = Calendar.getInstance();
		String dataFlag = yyyy_mm_dd.format(c.getTime());
		
		log.info("查询兴业商户信息");
		
		String sql = "select * from TBL_PAY_MERCHANTCHANNELCONFIG where channelId=? ";
		
		/**
		 *  and  superMerchantNo in 
		 *   (select superMerchantNo from tbl_online_order where left(createDate,10)='2017-04-05' and orderStatus='SUCCESS' GROUP BY superMerchantNo)
		 */
		
//		String sql = "select * from TBL_PAY_MERCHANTCHANNELCONFIG where channelId=? and"
//				+ " (superMerchantNo='101520034643' or superMerchantNo='101530036054'  or superMerchantNo='101530036053'"
//				+ "  or superMerchantNo='101550035781'  or superMerchantNo='101510034260'   or superMerchantNo='101570037761' )";
		
		log.info("查询兴业商户" + sql); 
		
		List<Map<String, Object>> merchantlist = queryForList(sql, new Object[]{channelName});

		log.info("需要对账的商户数量：" + merchantlist.size()); 
		
		List<Object[]> dblist = new ArrayList<Object[]>();
		
		String TBL_ONLINE_Reconciliation = "insert into TBL_ONLINE_Reconciliation (optimistic,bankOrderId,channelName,superMerchantNo,trxAmount,trxDate,trxFee,trxType,dateFlag)"
				+ " values (?,?,?,?,?,?,?,?,?)";
		
		for (Map<String, Object> map : merchantlist) {

			String superMerchantNo = map.get("superMerchantNo").toString().trim();
			String channelSign = map.get("channelSign").toString().trim();

			log.info("对账商户编号:" + superMerchantNo);
			
			SortedMap<Object, Object> params = createCheckAccountMessageForSign(date, superMerchantNo);
			log.info("兴业对账计算密钥的报文params为===========" + params.toString());
			// 计算sign值
			String sign = createSign("UTF-8", params, channelSign);
			log.info("兴业对账计算出的密钥为==========" + sign);
			String nonce_str = (String) params.get("nonce_str");
			// 制作对账报文
			String plainXML = createCheckAccountMessage(date, nonce_str, sign, superMerchantNo);

			log.info("兴业对账请求报文==================：" + plainXML);

			String payrequest_result = null;
			try {
				payrequest_result = HttpClient.xml(url, plainXML);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("兴业商户编号： " + superMerchantNo +"  ,对账返回报文=================" + payrequest_result);
			
			if(payrequest_result == null){
				continue;
			}

			List<Map<String,Object>> list = dealCheckAccountMessage(payrequest_result, channelName);
			
			log.info("兴业对账list的size的大小是================" + list.size());
            count += list.size();
            if (list.size() > 0) {
            	log.info("兴业list=============" + list.toString());
                for (Map<String,Object> s1 : list) {
                    sumAmount = sumAmount.add(new BigDecimal(s1.get("trxAmount").toString())) ;
                    sumFee = sumFee.add(new BigDecimal(s1.get("trxFee").toString()));
                    date1 = s1.get("trxDate")==null?date:s1.get("trxDate").toString();
                    log.info("兴业对账s1================" + s1.toString());
                    Object[] obj =  new Object[]{0,s1.get("bankOrderId"),channelName,superMerchantNo,s1.get("trxAmount"),
                    		date1 , s1.get("trxFee"),0 , dataFlag};
                    dblist.add(obj);
                }
            }
            if(dblist.size() > listSize){
            	log.info("list的长度大于" + listSize + "保存数据库");
            	executeBatchSql(TBL_ONLINE_Reconciliation, dblist);
            	dblist.clear();
            }
		}
		if(dblist.size() > 0){
			log.info("将list剩余数据保存数据库");
			executeBatchSql(TBL_ONLINE_Reconciliation, dblist);
		}
		
		Integer reconciliationSEQ = Integer.parseInt(queryForMap("select max(id)+1 as id from TBL_ONLINE_Reconciliation", null).get("id").toString());
		
		log.info("更新 sequence_online_reconciliation 序列  : " + reconciliationSEQ);
		executeSql("update sequence_online_reconciliation set next_val=?" , new Object[]{reconciliationSEQ}); 

		log.info("tbl_PAY_CHECKACCOUNTRECORD");
		String next_val = queryForMap("select * from sequence_PAY_CHECKACCOUNTRECORD", null).get("next_val").toString();
		sql = "insert into tbl_PAY_CHECKACCOUNTRECORD (id,optimistic,channelId,count,createDate,date,sumAmount,sumFee,trxFlag) values "
				+ "(?,?,?,?,?,?,?,?,?)";
		executeSql(sql, new Object[]{next_val,0,channelName,count,date1,date,sumAmount,sumFee,1});
		executeSql("update sequence_PAY_CHECKACCOUNTRECORD set next_val=? ", new Object[]{Integer.parseInt(next_val) + 1});
	}
	
	
	public void checkAccount(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		
		log.info("开始对账操作对账日期:" + date);
		
		String sql = "update TBL_ONLINE_Reconciliation as a , tbl_online_copyorder as b "
				+ " set b.checkAmount=a.trxAmount, b.checkFee=a.trxFee , b.checkScanCodeStatus='SUCCESS' , b.channelId=b.channelId"
				+ " where a.bankOrderId=b.bankOrderId and left(a.trxDate,10)='"+ date +"' and  left(b.createDate,10)='"+ date +"' and (b.checkScanCodeStatus!='LONG' or b.checkScanCodeStatus is null)";
		
		log.info("执行的sql ：" + sql);
		
		int x = executeSql(sql, null);
		log.info("对账完成，受影响行数：" +  x );
		
		log.info("处理长款数据"); 
		sql = "insert into tbl_online_copyorder (optimistic,bankOrderId , channelId , checkAmount , checkFee ,"
				+ " checkScanCodeStatus , checkWithdrawStatus,cost ,createDate, mode ,orderAmount, orderStatus ,"
				+ " payerFee,receiverFee ,superMerchantNo,t0PayResult,orderWithdrawAmount,orderWithdrawFee)"
				+ " select optimistic,bankOrderId,channelName,trxAmount,trxFee,'LONG','INIT', 0,trxDate, 0 ,0,'SUCCESS' ,0, 0 ,superMerchantNo,0,0,0"
				+ " from TBL_ONLINE_Reconciliation where  LEFT(trxDate,10)='" + date +"' and bankOrderId not in "
				+ "(select bankOrderId from tbl_online_copyorder where left(createDate,10)='" + date + "' and bankOrderId is not null )";
		
		log.info("处理长款数据, 执行sql" + sql); 
		
		x = executeSql(sql, null);
		log.info("处理长款数据完成，受影响行数：" +  x );
		
		sql = "select max(id)+1 as maxid from tbl_online_copyorder" ;
		String maxid = queryForMap(sql, null).get("maxid").toString();
		
		log.info("开始更新 tbl_online_copyorder 序列, 序列值为：" +  maxid);
		
		sql = "update sequence_online_copyorder set next_val=? ";
		executeSql(sql, new Object[]{maxid});
		
	}
	
	
	public void init(){
		Long start = System.currentTimeMillis();
		
		downloadXINGYEcheckAccount();
		checkAccount();
		
		Long end = System.currentTimeMillis();
		Long time = (end - start)/1000;
		log.info("对账结束 共使用时间:" + time + "秒 ");
	}
	
	public static void main(String[] args) {
		CheckAccountHandler checkAccount = new CheckAccountHandler();
		checkAccount.init();
	}
}
