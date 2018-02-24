package com.rhjf.balance;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.rhjf.base.OnlineBaseDao;

public class CopyWithdrawData  extends OnlineBaseDao{
	
	
	public void CopyWithdraw(){
		
		Long start = System.currentTimeMillis();
		
		log.info("开始拷贝数据 , 开始时间:" +  start);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE , -1);
		String date = sdf.format(c.getTime()); 
		
		
		
		String insertCopyOrder = "insert into tbl_online_withdraw_order_copy (optimistic,agentNo,bankName,bankOrderId,cardNo,createDate,desciption,idCardNo,"
				+ " orderNumber,payerName,receiverFee,trxType,withdrawAmount,withdrawMsg,withdrawStatus,bankBranch,bankCode,linkPhone,checkAccountStatus) "
				+ " select optimistic,agentNo,bankName,bankOrderId,cardNo,createDate,desciption,idCardNo,orderNumber,payerName,receiverFee,"
				+ " trxType,withdrawAmount,withdrawMsg,withdrawStatus,bankBranch,bankCode,linkPhone,'INIT' from tbl_online_withdraw_order "
				+ " where date(createDate)='"+date+"' and withdrawStatus='success' ";
		
		log.info("执行的sql：" + insertCopyOrder);
		
		int x = executeSql(insertCopyOrder, null);



		String maxID = "select max(id) as id  from tbl_online_withdraw_order_copy";

		Integer id = Integer.parseInt(queryForMap(maxID , null).get("id").toString())+1;

		log.info("查询的最大ID：" + id);

		String updateSEQ = "update sequence_online_withdraw_order_copy set next_val=" + id;
		executeSql(updateSEQ , null);

		
		log.info("转移完成，受影响行数：" +  x );
		
		Long end = System.currentTimeMillis();
		Long time = (end - start)/1000;
		
		log.info("共使用时间:" + time + "秒 ");
		
	}
	

}
