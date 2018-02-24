package com.rhjf.merchant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhjf.base.BaseDao;

/** 
 *    定时任务
 *     	每10分钟执行一次，
 *      如果商户当天的交易金额达到一定上限 将暂时停止该商户，等到第二天再开启商户交易
 * @author hadoop
 *
 */
public class WXMerchant extends BaseDao {
	
	// 当天最大交易金额                                                                 2147483647
	private static final int MAX_TRADE_AMOUNT = 28000000;
	
	public void init(){
		
		log.info("任务开始:");
		log.info("===========查询商户目前的交易金额:");
		String sql = "select wx.PlatMerchantID , wx.MerchantName , SUM(ts.Amount) as Amount , wx.WxKey "
				+ " from tab_wx_merchant as wx, tab_support as ts "
				+ " where ts.PlatMerchantID=wx.PlatMerchantID and appidValid=1 and valid=1 and ts.PayRetCode='00'"
				+ " GROUP BY PlatMerchantID order by amount desc";
		List<Map<String,String>> list = queryForList(sql);
		log.info("===========目前可交易商户数量为：" + list.size() + ",目前最大金额:" + list.get(0).get("Amount"));
		List<Map<String,String>> needStopMerchant = new ArrayList<Map<String,String>>();
		
		for (Map<String,String> map : list) {
			int amount = Integer.parseInt(map.get("Amount"));
			// 商户交易金额大于限制金额
			if(amount > MAX_TRADE_AMOUNT){
				String PlatMerchantID = map.get("PlatMerchantID");
				
				log.info("商户名称:" + map.get("MerchantName") 
						+ " ,商户编号:" + PlatMerchantID
						+ " ,目前交易金额:" +  map.get("Amount"));
				
				Map<String,String> merchantMap = new HashMap<String,String>();
				merchantMap.put("PlatMerchantID", PlatMerchantID);
				merchantMap.put("MerchantName",map.get("MerchantName"));
				merchantMap.put("WxKey", map.get("WxKey"));
				needStopMerchant.add(merchantMap);
			}
		}
		log.info("===========需要暂停的数量:" + needStopMerchant.size());
		
		for (Map<String,String> map : needStopMerchant) {
			String platMerchantID = map.get("PlatMerchantID");
			log.info("======================需要暂停的商户号:" + platMerchantID);

			sql = "insert into tab_wx_merchant_tem (PlatMerchantID,WxKey ,MerchantName) values (?,?,?)";
			int x = executeSql(sql, new Object[]{platMerchantID,
					map.get("WxKey"),map.get("MerchantName")});
			if(x > 0){
				sql = "update tab_wx_merchant set valid=0,appidValid=0 where PlatMerchantID=?";
				int ref = executeSql(sql, new Object[]{platMerchantID});
				if(ref > 0){
					log.info("======================商户号:" + platMerchantID + "暂停成功");
				}else{
					log.info("======================platMerchantID:" + platMerchantID + "pause fails");
				}
			}
		}
		log.info("任务结束!");
	}
	public static void main(String[] args) {
		WXMerchant wxMerchant = new WXMerchant();
		wxMerchant.init();
	}
}
