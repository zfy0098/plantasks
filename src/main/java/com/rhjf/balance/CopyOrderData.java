package com.rhjf.balance;

import java.text.SimpleDateFormat;  
import java.util.Calendar;

import com.rhjf.base.OnlineBaseDao;
import org.apache.log4j.Logger;




public class CopyOrderData extends OnlineBaseDao{

	Logger log = Logger.getLogger(this.getClass());
	
	
	public void CopyOrder(){
		
		Long start = System.currentTimeMillis();
		
		log.info("开始拷贝数据 , 开始时间:" +  start);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE , -1);
		String date = sdf.format(c.getTime()); 
		// checkScanCodeStatus,checkWithdrawStatus  
		// left(createDate,13) BETWEEN '2017-03-30 08' and '2017-03-30 14'"
		String insertCopyOrder = " insert into tbl_online_copyorder  (optimistic,b2bOrB2c,bankId,debitOrCredit,channelCompleteDate,kuaiType,"
				+ "merchantNo,orderAmount,receiverFee,orderIp,orderNumber,createDate,completeDate,bankOrderId,orderStatus,"
				+ "payType,payerFee, trxType,withdrawStatus,cost,mode,t0PayResult,merchantId,withdrawAmount,withdrawFee,superMerchantNo,"
				+ "channelId,withdrawNo,withdrawMsg,checkScanCodeStatus,checkWithdrawStatus , withdrawTime , agentCost) "
				+ "select optimistic,b2bOrB2c,bankId,debitOrCredit,channelCompleteDate,kuaiType,"
				+ "merchantNo,orderAmount,receiverFee,orderIp,orderNumber,createDate,completeDate,bankOrderId,orderStatus,"
				+ "payType,payerFee, trxType , withdrawStatus,cost,mode,t0PayResult,merchantId,withdrawAmount,withdrawFee,superMerchantNo,"
				+ "channelId,withdrawNo,withdrawMsg , 'INIT' , 'INIT' , '"+date+"' , agentCost from tbl_online_order where orderStatus='SUCCESS' and  LEFT(createDate,10)='" + date + "' "
				+ "  and channelId!='rytPayKuai' and  channelId!='UNIONPAYQRCODE'	";
		
		log.info("执行的sql：" + insertCopyOrder);
		
		int x = executeSql(insertCopyOrder, null);
		
		
		log.info("转移完成，受影响行数：" +  x );
		
		
		String sql = "select max(id)+1 as maxid from tbl_online_copyorder" ;
		String maxid = queryForMap(sql, null).get("maxid").toString();
		
		log.info("开始更新序列, 序列值为：" +  maxid);
		
		sql = "update sequence_online_copyorder set next_val=? ";
		executeSql(sql, new Object[]{maxid});
		
		Long end = System.currentTimeMillis();
		Long time = (end - start)/1000;
		
		log.info("共使用时间:" + time + "秒 ");
	}
	
	
	public static void main(String[] args) {
		CopyOrderData copy = new CopyOrderData();
		copy.CopyOrder();

		CopyKuaiData copyKuaiData = new CopyKuaiData();
		copyKuaiData.CopyOrder();

	}
}
