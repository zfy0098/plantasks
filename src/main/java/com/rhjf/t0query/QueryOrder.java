package com.rhjf.t0query;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.MD5;
import com.rhjf.utils.UtilsConstant;

import net.sf.json.JSONObject;

public class QueryOrder extends BaseDao {

	Logger logger = Logger.getLogger(this.getClass());
	
	
	private static final String queryURL = "http://trx.ronghuijinfubj.com/middlepaytrx/online/query";
	
	
	public void payQuery(){
		
//		String queryOrder = "SELECT * from tab_pay_order "
//				+ " where LocalTimes > DATE_FORMAT(NOW()-INTERVAL 2 HOUR , '%H%i%S') and LocalDate=DATE_FORMAT(now(),'%Y%m%d')"
//				+ " and (PayRetCode is null or PayRetCode != 0000) and TradeType='扫码支付'";
		
		String queryOrder = "select * from tab_pay_order where  OrderNumber in ('20170824205103960303735')";
		
		List<Map<String,Object>> orderlist = queryForList(queryOrder,null);
		
		logger.info("订单没有确定支付成功的条数 ：" + orderlist.size());
		
		for (Map<String, Object> orderMap : orderlist) {
			
			logger.info("查询订单:" + orderMap.get("OrderNumber") + "支付状态"); 
			
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
					
					String orderStatus = json.getString("r8_orderStatus");
					
					/** 支付成功 **/
					if("SUCCESS".equals(orderStatus)){
						NotifyService notifyService = new NotifyService();
						
						PayOrder order = UtilsConstant.mapToBean(orderMap, PayOrder.class);
						
						String userinfo = "select * from tab_loginuser where ID=?";
						TabLoginuser user = UtilsConstant.mapToBean(queryForMap(userinfo, new Object[]{order.getUserID()}), TabLoginuser.class);
						
						Fee fee = null;
						if("固定码".equals(order.getTradeType())){
							String qrcodesql = "select * from tab_ymf_qrcode where Code=?";
							Map<String,Object> qrcode =  queryForMap(qrcodesql, new Object[]{order.getYMFCode()});
							
							fee = notifyService.YMFcalProfit(order, user, qrcode);
						}else{
							fee = notifyService.calProfit(order, user);
						}
						
						
						
						String updateOrderStatusSQL = "update tab_pay_order set PayRetCode=? , PayRetMsg=? , Fee=? , MerchantProfit=?  where ID=?";
						
						executeSql(updateOrderStatusSQL, new Object[]{retCode ,"支付成功" ,fee.getMerchantFee() , fee.getMerchantprofit() , order.getID()});
						
						String sql = "insert into tab_platform_profit (ID,UserID,TradeID,Fee,AgentID,AgentProfit,TwoAgentID,TwoAgentProfit,DistributeProfit,PlatformProfit,ChannelProfit)"
								+ " values (?,?,?,?,?,?,?,?,?,?,?)";
						int x = executeSql(sql, new Object[] { UtilsConstant.getUUID(), user.getID(), order.getID(),
										fee.getMerchantFee(), fee.getAgentID(), fee.getAgentProfit(),
										fee.getTwoAgentID(), fee.getTwoAgentProfit(), fee.getDistributeProfit(),
										fee.getPlatformProfit(), fee.getPlatCostFee() });
						
						if(x < 1){
						     continue;
						}
						
						/**  计算三积分销各个商户的利润   **/
						List<Object[]> objs = notifyService.calDistributeProfit(fee, order, user);
						
						logger.info("订单号：" + order.getOrderNumber() + "三级分销的list长度:" + objs.size());
						
						if(objs.size()>0){
							/**  保存三级分销各个商户的利润  **/
							String saveDistributeProfitsql = "insert into tab_user_profit (ID,UserID,Amount,TradeTime,TradeID) "
									+ "values (?,?,?,?,?)";
							
							executeBatchSql(saveDistributeProfitsql , objs);
							/** 更新用户信息表中的 分润总额 **/
							List<Object[]> profitlist = new ArrayList<Object[]>();
							for (Object[] objects : objs) {
								
								Integer profit = Integer.parseInt(objects[2].toString());
								
								// 如果三积分销中的用户包含业务员
								if(fee.getSalemsManID()!= null){
									if(objects[1].equals(fee.getSalemsManID())){
										
										logger.info("订单：" + order.getOrderNumber() + "交易三级分销包含业务员 " + profit);
										
										fee.setSalemsDistrubuteProfit(profit); 
										profit += fee.getSalemsGetAgentProfit();
									}
								} else {
									logger.info("订单：" + order.getOrderNumber() + " 交易三级分销没有业务员  " + profit);
								}
								
								logger.info("交易单号：" + order.getOrderNumber() + "  ====用户分润list中的值：" + Arrays.toString(objects)); 
								profitlist.add(new Object[]{ profit , profit , objects[1]});
							}
							String updateuserprofit = "update tab_loginuser set FeeAmount=FeeAmount+?  ,  FeeBalance=FeeBalance+? where ID=?";
							executeBatchSql(updateuserprofit , profitlist);
						} else {
							logger.info("订单号：" +  order.getOrderNumber() + ",没有产生分润");
						}
						
						if(fee.getSalemsManID()!= null){
							logger.info("订单号：" + order.getOrderNumber() + " 交易商户 涉及业务员分润"); 
							
							String saveSalesManProfit = "insert into tab_salesman_profit (ID,SalesManID,TradeUserID,TradeID,DistributeProfit,Profit,TradeDate)"
									+ " values (?,?,?,?,?,?,now())";
							executeSql(saveSalesManProfit, new Object[]{UtilsConstant.getUUID(),fee.getSalemsManID(),order.getUserID(),order.getID()
									,fee.getSalemsDistrubuteProfit(),fee.getSalemsGetAgentProfit()});
						}
						
					}
					
					String withdrawStatus = json.getString("r9_withdrawStatus");
					/** 提现状态成功  **/
					if("SUCCESS".equals(withdrawStatus)){
						logger.info("订单:" + orderMap.get("OrderNumber") + "出款成功");
						
						/** 提现状态成功  **/
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
		QueryOrder queryorder = new QueryOrder();
		queryorder.payQuery();
	}
}
