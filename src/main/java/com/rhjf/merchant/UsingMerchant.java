package com.rhjf.merchant;

import java.util.List;
import java.util.Map;

import com.rhjf.base.BaseDao;
import com.rhjf.pay.Amount;

public class UsingMerchant extends BaseDao{

	public void init(){
		log.info("开始恢复上一天停用的商户:");
		String sql = "select PlatMerchantID , MerchantName from tab_wx_merchant_tem";
		List<Map<String , String>> list = queryForList(sql);
		log.info("===========================需要恢复的商户数量:" + list.size()); 

		Amount amount = new Amount();
		
		for (Map<String, String> map : list) {
			String platMerchantID = map.get("PlatMerchantID");
			String merchantName = map.get("MerchantName");
			
			sql = "update tab_wx_merchant set appidValid=1 , valid=1 where PlatMerchantID=?";
			int x = executeSql(sql, new Object[]{platMerchantID});
			if(x > 0){
				log.info("=========================================" + merchantName + ",启动成功");
				log.info("=========================================删除商户" + platMerchantID + "在临时表的数据");
				sql = "delete from tab_wx_merchant_tem where PlatMerchantID=?";
				int ref = executeSql(sql , new Object[]{platMerchantID});
				if(ref > 0){
					log.info("=========================================删除临时数据成功 , 商户号:"+platMerchantID);
				}else{
					log.info("=========================================delete ephemeral data fails , merchantid:" +platMerchantID);
					amount.sendMail("delete ephemeral data fails , merchantid:" +platMerchantID);
				}
			}else{
				log.info("========================================= merchatnName:" + merchantName + ",start fails ,merchantid:" + platMerchantID);
				amount.sendMail("merchantName:" + merchantName + ",start fails ,merchantid:" + platMerchantID);
 			}
		}
		
		log.info("任务结束");
	}
	
	
	public static void main(String[] args) {
		UsingMerchant usingMerchant = new UsingMerchant();
		usingMerchant.init();
	}
	
}
