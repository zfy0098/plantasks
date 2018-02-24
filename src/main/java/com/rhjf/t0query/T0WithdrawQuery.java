package com.rhjf.t0query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.MD5;

import net.sf.json.JSONObject;

/**
 *    app t0交易出款查询
 * @author a
 *
 */

public class T0WithdrawQuery extends BaseDao{

	Logger logger = Logger.getLogger(this.getClass());
	
	
	private static final String queryURL = "http://10.10.20.103:11124/middlepaytrx/online/query";
	
	
	public void payQuery(){
		
//		String queryOrder = "SELECT * from tab_pay_order where LocalDate=DATE_FORMAT(now(),'%Y%m%d') and PayRetCode='0000' and (T0PayRetCode is null or T0PayRetCode='INIT')";

		String queryOrder = "select * from tab_pay_order where PayRetCode='0000' and T0PayRetCode is null and TradeCode='T0'";


//		String queryOrder = "select * from tab_pay_order where ordernumber = '20180108142927707956300'";

		logger.info("查询t0支付未出款订单,执行sql：" + queryOrder);
		
		List<Map<String,Object>> orderlist = queryForList(queryOrder,null);
		
		logger.info("t0支付未出款订单条数：" + orderlist.size());
		
		for (Map<String, Object> orderMap : orderlist) {
			
			
			logger.info("查询订单:" + orderMap.get("OrderNumber") + "代付状态"); 
			
			String queryMerchant = "select * from tab_pay_merchant where MerchantID=?";
			Map<String,Object> merchantMap = queryForMap(queryMerchant, new Object[]{orderMap.get("MerchantID").toString()});
			
			String signkey = merchantMap.get("QueryKey").toString();
			
			Map<String,Object> map = new LinkedHashMap<String,Object>();
			
			map.put("trxType", "OnlineQuery");
			map.put("r1_merchantNo", orderMap.get("MerchantID"));
			map.put("r2_orderNumber", orderMap.get("OrderNumber"));
			
			StringBuffer sbf = new StringBuffer("#");
			for (String key : map.keySet()) {
				sbf.append(map.get(key));
				sbf.append("#");
			}
			
			String str = sbf.append(signkey).toString();
			
			map.put("sign", MD5.sign(str, "utf-8"));
			
			try {
				String content = HttpClient.post(queryURL, map, "1");
				logger.info("订单:" + orderMap.get("OrderNumber") + "响应报文:" + content);
				
				JSONObject json = JSONObject.fromObject(content);
				
				String retCode = json.getString("retCode");
				
				if("0000".equals(retCode)){
					
					String withdrawStatus = json.getString("r9_withdrawStatus");
					
					/** 提现状态成功  **/
					if("SUCCESS".equals(withdrawStatus)){
						logger.info("订单:" + orderMap.get("OrderNumber") + "出款成功");
						String sql = "update tab_pay_order set T0PayRetCode=? , T0PayRetMsg=? where ID=? ";
						executeSql(sql, new Object[]{ retCode , "成功-承兑或交易成功" ,orderMap.get("ID")});
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		T0WithdrawQuery withdrawquery = new T0WithdrawQuery();
		withdrawquery.payQuery();
	}
}
