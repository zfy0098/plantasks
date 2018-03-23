package com.rhjf.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.DateUtil;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.MD5;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AppCheck extends BaseDao {

	private static final String URL = "http://10.10.20.101:11024/middlepayportal/merchant/merchantCheckAccount";

	private static final String REPORT_SIGN_KEY = "212876ea28cc11e7b8590894ef29bfa8";

	public void init() throws Exception {

		String yesterday = DateUtil.getDateAgo(DateUtil.getNowTime(DateUtil.yyyy_MM_dd), 1, DateUtil.yyyy_MM_dd);

		String copyOrderSql = "insert into tab_pay_copyorder "
				+ " (ID , amount , tradedate,tradetime , termserno , tradetype,tradecode , userid, ChannelID ,paychannel,feerate  ,merchantid , fee,ordernumber) "
				+ " select ID , amount , tradedate,tradetime , termserno , tradetype,tradecode , userid, ChannelID ,paychannel,feerate,merchantid, fee,ordernumber"
				+ " from tab_pay_order where tradedate=? and PayRetCode='0000' ";
		
		executeSql(copyOrderSql, new Object[]{yesterday.replace("-", "")});
		

		String sql = "select MerchantID from tab_pay_order where LocalDate=? GROUP BY MerchantID";
		
		List<Map<String,Object>> merchantList = queryForList(sql, new Object[]{yesterday.replace("-", "")});
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		String saveReconciliation = "insert into tab_order_reconciliation (ID,MerchantID,OrderNumber,CheckFee,CheckAmount,CheckOrderStatus,CheckWithdrawStatus,TradeDate) "
				+ " values (?,?,?,?,?,?,?,?)";
		
		for (int i = 0; i < merchantList.size(); i++) {
			
			Map<String, Object> map = new TreeMap<String, Object>();
			map.put("merchantNo", merchantList.get(i).get("MerchantID"));
			map.put("date", yesterday);
			JSONObject json = JSONObject.fromObject(map);
			String sign = MD5.sign(json.toString() + REPORT_SIGN_KEY, "utf-8").toUpperCase();

			map.put("sign", sign);

			String content = HttpClient.post(URL, map, null);
			
			log.info("请求对账接口返回报文：" + content);
			JSONObject result = JSONObject.fromObject(content);
			
			if("0000".equals(result.getString("respCode"))){
				JSONArray data = result.getJSONArray("data");
				
				for (int j = 0; j < data.size(); j++) {
					JSONObject tradeData = data.getJSONObject(j);
					
					String ID = UUID.randomUUID().toString().toUpperCase();
					String MerchantID = merchantList.get(i).get("MerchantID").toString();
					String orderNumber = tradeData.getString("orderNumber");
					Integer receiverFee = (int) AmountUtil.mul(String.valueOf(tradeData.getDouble("receiverFee")), "100");
					Integer orderAmount = (int) AmountUtil.mul(String.valueOf(tradeData.getDouble("orderAmount")) ,"100");
					String orderStatus = "SUCCESS".equals(tradeData.getString("orderStatus"))?"0000":"";
					String withdrawStatus = "SUCCESS".equals(tradeData.getString("withdrawStatus"))?"0000":"";
					
					Object[] obj = new Object[]{ID,MerchantID , orderNumber , receiverFee ,orderAmount, orderStatus ,withdrawStatus , yesterday};
					list.add(obj);
				}
			}
		}
		executeBatchSql(saveReconciliation, list);
		
		/* 执行对账操作 */
		String checkSql = "update tab_pay_copyorder as tpc , tab_order_reconciliation as tor "
				+ " set tpc.CheckAmount=tor.CheckAmount , tpc.CheckFee=tor.CheckFee , tpc.CheckStatus=tor.CheckOrderStatus , tpc.T0CheckStatus=tor.CheckWithdrawStatus"
				+ " where tpc.OrderNumber=tor.OrderNumber and tor.TradeDate=?  and tpc.ChannelID='RONGHUI'";
		executeSql(checkSql, new Object[]{yesterday});
		
		
		/*处理长款数据  */
		String longTradeSql = "insert into tab_pay_copyorder "
				+ " (ID , amount , tradedate,tradetime , termserno , tradetype,tradecode   ,merchantid , fee,ordernumber , CheckAmount , CheckFee,CheckStatus,T0CheckStatus , ChannelID) "
				+ " select   UPPER(UUID()) , 0 , ? , '' , '' , '' , '' , MerchantID ,0 , OrderNumber , CheckAmount , CheckFee , '1000' , '' , 'RONGHUI' "
				+ " from tab_order_reconciliation where OrderNumber not in (select OrderNumber from tab_pay_copyorder where TradeDate=?) and TradeDate=?";
		executeSql(longTradeSql, new Object[]{yesterday.replace("-", "") , yesterday.replace("-", "") , yesterday});
		
		
		String updateUserID = "update tab_pay_copyorder as tpco, tab_pay_order as tpo set tpco.userID = tpo.userID , tpco.PayChannel=tpo.payChannel "
				+ "where  tpco.UserID is null and tpco=ChannelID='RONGHUI'";
		
		executeSql(updateUserID, null);
	}

	public static void main(String[] args) throws Exception {
		AppCheck appCheck = new AppCheck();
		appCheck.init();
	}



	public void yichang(){


		String yesterday = "2018-01-26";

		String sql = "select MerchantID from tab_pay_order where LocalDate=? GROUP BY MerchantID";

		List<Map<String,Object>> merchantList = queryForList(sql, new Object[]{yesterday.replace("-", "")});

		List<Object[]> list = new ArrayList<Object[]>();

		String saveReconciliation = "insert into tab_order_reconciliation (ID,MerchantID,OrderNumber,CheckFee,CheckAmount,CheckOrderStatus,CheckWithdrawStatus,TradeDate) "
				+ " values (?,?,?,?,?,?,?,?)";

		for (int i = 0; i < merchantList.size(); i++) {

			Map<String, Object> map = new TreeMap<String, Object>();
			map.put("merchantNo", merchantList.get(i).get("MerchantID"));
			map.put("date", yesterday);
			JSONObject json = JSONObject.fromObject(map);
			String sign = MD5.sign(json.toString() + REPORT_SIGN_KEY, "utf-8").toUpperCase();

			map.put("sign", sign);

			String content = HttpClient.post(URL, map, null);

			log.info("请求对账接口返回报文：" + content);
			JSONObject result = JSONObject.fromObject(content);

			if("0000".equals(result.getString("respCode"))){
				JSONArray data = result.getJSONArray("data");

				for (int j = 0; j < data.size(); j++) {
					JSONObject tradeData = data.getJSONObject(j);

					String ID = UUID.randomUUID().toString().toUpperCase();
					String MerchantID = merchantList.get(i).get("MerchantID").toString();
					String orderNumber = tradeData.getString("orderNumber");
					Integer receiverFee = (int) AmountUtil.mul(String.valueOf(tradeData.getDouble("receiverFee")), "100");
					Integer orderAmount = (int) AmountUtil.mul(String.valueOf(tradeData.getDouble("orderAmount")) ,"100");
					String orderStatus = "SUCCESS".equals(tradeData.getString("orderStatus"))?"0000":"";
					String withdrawStatus = "SUCCESS".equals(tradeData.getString("withdrawStatus"))?"0000":"";

					Object[] obj = new Object[]{ID,MerchantID , orderNumber , receiverFee ,orderAmount, orderStatus ,withdrawStatus , yesterday};
					list.add(obj);
				}
			}
		}
		executeBatchSql(saveReconciliation, list);

		/* 执行对账操作 */
		String checkSql = "update tab_pay_copyorder as tpc , tab_order_reconciliation as tor "
				+ " set tpc.CheckAmount=tor.CheckAmount , tpc.CheckFee=tor.CheckFee , tpc.CheckStatus=tor.CheckOrderStatus , tpc.T0CheckStatus=tor.CheckWithdrawStatus"
				+ " where tpc.OrderNumber=tor.OrderNumber and tor.TradeDate=?";
		executeSql(checkSql, new Object[]{yesterday});


		/* 处理长款数据  */
		String longTradeSql = "insert into tab_pay_copyorder "
				+ " (ID , amount , tradedate,tradetime , termserno , tradetype,tradecode   ,merchantid , fee,ordernumber , CheckAmount , CheckFee,CheckStatus,T0CheckStatus) "
				+ " select   UPPER(UUID()) , 0 , ? , '' , '' , '' , '' , MerchantID ,0 , OrderNumber , CheckAmount , CheckFee , '1000' , '' "
				+ " from tab_order_reconciliation where OrderNumber not in (select OrderNumber from tab_pay_copyorder where TradeDate=?) and TradeDate=?";
		executeSql(longTradeSql, new Object[]{yesterday.replace("-", "") , yesterday.replace("-", "") , yesterday});


		String updateUserID = "update tab_pay_copyorder as tpco, tab_pay_order as tpo set tpco.userID = tpo.userID , tpco.PayChannel=tpo.payChannel "
				+ "where  tpco.UserID is null";

		executeSql(updateUserID, null);
	}

}
