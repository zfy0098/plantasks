package com.rhjf.t0query;

import java.text.DecimalFormat; 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;





public class NotifyService extends BaseDao{
	
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 *   计算手续费
	 * @param order
	 * @param loginUser
	 * @return
	 */
	public Fee calProfit(PayOrder order , TabLoginuser loginUser ){
		
		log.info("计算订单：" + order.getOrderNumber() + "手续费");
		
		Fee fee = new Fee();
		String tradeCode = order.getTradeCode();
		// 交易费率
		String feeRate = "0";
		// 结算费率
		String SettlementRate = "0";
		// t0 附加费用
		int T0additional = 0;
		
		
		boolean flag = false;
		
		Map<String,Object> salemsMan  = null;
		/**  交易商户 的业务员ID **/
		String salemsManID = loginUser.getSalesManID();
		if(salemsManID != null && !salemsManID.trim().isEmpty()){
			flag = true;
			String salesmaninfosql = "select * from tab_salesman where ID=?";
			salemsMan = queryForMap(salesmaninfosql, new Object[]{salemsManID});
			if(salemsMan == null || salemsMan.isEmpty()){
				flag = false;
			}else{
				fee.setSalemsManID(salemsMan.get("ID").toString()); 
				log.info("计算订单：" + order.getOrderNumber() + " 包含业务员  ， 业务员ID：" + salemsMan.get("ID").toString());
			}
 		}
		
		
		/** 用户费率配置信息 **/
		String userconfigsql =  "select * from tab_user_config where UserID=? and PayChannel=?";
		Map<String,Object> userConfig = queryForMap(userconfigsql, new Object[]{order.getUserID() ,  order.getPayChannel()});
		
		/** 代理商信息  **/
		String agentinfosql = "select * from tab_agent where ID=?";
		Map<String,Object> agentInfo = queryForMap(agentinfosql, new Object[]{loginUser.getAgentID()});
		
		/**  代理商配置信息  **/
		String agentconfigsql = "select * from tab_agent_config where AgentID=? and PayChannel=?";
		Map<String,Object> agentConfig = queryForMap(agentconfigsql, new Object[]{agentInfo.get("ID") , order.getPayChannel()});
		
		if(agentConfig==null||agentConfig.isEmpty()){
			log.info("代理商：" + agentInfo.get("ID") + "没有配置支付类型：" +  order.getPayChannel()); 
			return null;
		}
		
		
		/**  商户下放费率   ,   代理商签约成本 ,  渠道成本  **/
		String merchantRate = "0", agentRate = "0" ,  channelRate ="0";
		
		/** 业务员返利点 **/
		String salesManRate = "0";
		
		
		/** 交易类型为T0 **/
		if(tradeCode.equals("T0")){
			
			log.info("订单:" + order.getOrderNumber() + "为T0交易"); 
			
			//  交易费率  T0SaleRate,T0SettlementRate 
			feeRate =  ObjToStr(userConfig.get("T0SaleRate"));
			//  结算费率                                                                                                    
			SettlementRate = ObjToStr(userConfig.get("T0SettlementRate"));
			//  T0商户下放
			merchantRate = ObjToStr(agentConfig.get("T0MerchantRate"));
			//  T0代理商成本
			agentRate = ObjToStr(agentConfig.get("T0AgentRate"));
			//  T0渠道成本
			channelRate = ObjToStr(agentConfig.get("T0ChannelRate"));
			
			//  T0附加手续费
			String t0additionalsql = "select * from tab_appconfig";
			T0additional = Integer.parseInt(ObjToStr(queryForMap(t0additionalsql, null).get("T0AttachFee")));
			
			
			if(flag){
				
				//  存在业务员信息
				salesManRate = salemsMan.get("T0FeeRate").toString();
				
				log.info("业务员费率值：" + salesManRate );
			}
			
			log.info(order.getOrderNumber() + "交易费率为：" + feeRate + ",结算费率为：" + SettlementRate + ",T0商户下放：" + merchantRate  + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}else{
			
			log.info("订单:" + order.getOrderNumber() + "为T1交易"); 
			
			/** 交易类型为T1 **/
			// 交易费率
			feeRate = ObjToStr(userConfig.get("T1SaleRate"));
			// 结算费率
			SettlementRate = ObjToStr(userConfig.get("T1SettlementRate"));
			//  商户下放费率
			merchantRate = ObjToStr(agentConfig.get("MerchantRate"));
			// 代理商成本费率
			agentRate = ObjToStr(agentConfig.get("AgentRate"));
			//  渠道成本
			channelRate = ObjToStr(agentConfig.get("ChannelRate"));
			
			
			if(flag){
				
				//  存在业务员信息
				salesManRate = salemsMan.get("FeeRate").toString();
				
				log.info("业务员费率值：" + salesManRate );
			}
			
			log.info(order.getOrderNumber()  + "交易费率为：" + feeRate + ",结算费率为：" + SettlementRate + ",商户下放：" + merchantRate  + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}
		
		/** 商户手续费 **/
		fee.setMerchantFee(makeFeeRounding(order.getAmount(), Double.valueOf(feeRate)  , 0) + T0additional);
		/** 商户自己的分润  **/
		fee.setMerchantprofit(makeFeeAbandon(order.getAmount(),AmountUtil.sub(feeRate, SettlementRate), 0));
		
		/** 三级分销总金额  **/
		int distributeProfit = makeFeeAbandon(order.getAmount(),AmountUtil.sub(SettlementRate, merchantRate), 0);
		fee.setDistributeProfit(distributeProfit);
		
		/** 二级代理商商户交易 **/
		if(agentInfo.get("ParentAgentID")!=null&&!agentInfo.get("ParentAgentID").equals(agentInfo.get("ID"))){
			
			log.info(order.getOrderNumber() + "订单的商户的代理商为二级代理商");
			
			//  获得父级代理商ID
			/** 代理商信息  **/
			String agentParentsql = "select * from tab_agent where ID=?";
			Map<String,Object> agentParent =queryForMap(agentParentsql , new Object[]{agentInfo.get("ParentAgentID")});
			
			//  获取父类代理商配置信息
			String agentParentConfigsql = "select * from tab_agent_config where AgentID=? and PayChannel=?";
			Map<String,Object> agentParentConfig = queryForMap(agentParentConfigsql , new Object[]{agentParent.get("ID") , order.getPayChannel()});
			//  代理商ID
			fee.setAgentID(agentParent.get("ID").toString());
			//  二级代理商ID
			fee.setTwoAgentID(agentInfo.get("ID").toString());
			
			String parentAgentRate = "0";
			
			if(tradeCode.equals("T0")){
				parentAgentRate = agentParentConfig.get("T0AgentRate").toString();
			}else{
				parentAgentRate = agentParentConfig.get("AgentRate").toString();
			}
			
			if(flag){
				//  如果存在业务员
				int salemsGetAgentProfit = makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,salesManRate) ,0);
				
				log.info("订单："+order.getOrderNumber() + "从代理商获取的收益为： " + salemsGetAgentProfit);
				fee.setSalemsGetAgentProfit(salemsGetAgentProfit);
				merchantRate = salesManRate;
			}
			
			//  代理商分润
			fee.setAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(agentRate ,parentAgentRate ) ,0));
			//  设置二级代理商分润
			fee.setTwoAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate), 0));
			agentRate = parentAgentRate;
		}else{
			log.info(order.getOrderNumber() + "订单商户的代理商为一级代理商");
			
			if(flag){
				//  如果存在业务员
				int salemsGetAgentProfit = makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,salesManRate) ,0);
				fee.setSalemsGetAgentProfit(salemsGetAgentProfit);
				
				log.info("订单："+order.getOrderNumber() + "从代理商获取的收益为： " + salemsGetAgentProfit);
				
				merchantRate = salesManRate;
			}
			
			
			// 代理商ID
			fee.setAgentID(agentInfo.get("ID").toString());
			//  代理商分润
			fee.setAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate) ,0));
		}
		//计算平台手续费
		fee.setPlatCostFee(makeFeeFurther(order.getAmount(), Double.valueOf(channelRate),0));
		// 平台收益
		fee.setPlatformProfit(makeFeeFurther(order.getAmount(),  AmountUtil.sub(agentRate, channelRate),0));
		
		return fee;
	}
	
	
	
	/**
	 *     计算三级分销
	 * @param fee
	 * @param loginUser
	 * @return
	 */
	public List<Object[]> calDistributeProfit( Fee fee , PayOrder order ,TabLoginuser loginUser){
		log.info("开始计算订单编号为：" + order.getOrderNumber() + "的三级分销分润");
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		String one = loginUser.getOneLevel();
		
		String two = loginUser.getTwoLevel();
		
		String three = loginUser.getThreeLevel();
		
		String agentinfosql = "select * from tab_agent where ID=?";
		
		Map<String,Object> agentInfo = queryForMap(agentinfosql, new Object[]{loginUser.getAgentID()});
 		
		int total = fee.getDistributeProfit();

		log.info("计算订单编号为：" + order.getOrderNumber() + "的三级分销分润总金额：" + total);
		Object[] obj = null;
		if(total > 0){
			/** 上级用户 **/
			if(!strIsEmpty(three)){
				int threeProfit = total*Integer.parseInt(ObjToStr(agentInfo.get("ThreeLeveFee")))/10;
				if(threeProfit > 0){
					obj = new Object[]{getUUID(),three,threeProfit ,order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				log.info("订单编号:" + order.getOrderNumber() + " , 上级用户" + three + "获得分润:" + threeProfit);
			}
			/** 第二级用户  **/
			if(!strIsEmpty(two)){
				int twoProfit = total*Integer.parseInt(ObjToStr(agentInfo.get("TwoLeveFee")))/10;
				if(twoProfit > 0){
					
					obj = new Object[]{getUUID(),two ,twoProfit ,order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				log.info("订单编号:" + order.getOrderNumber() + " , 第二级用户" + two + "获得分润:" + twoProfit);
			}
			
			/** 第三级用户  **/
			if(!strIsEmpty(one)){
				int oneProfit = total*Integer.parseInt(ObjToStr(agentInfo.get("OneLeveFee")))/10;
				if(oneProfit > 0){
					
					obj = new Object[]{getUUID(),one ,oneProfit , order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				log.info("订单编号:" + order.getOrderNumber() + " , 第三级用户" + one + "获得分润:" + oneProfit);
			}
		}
		
		if(fee.getMerchantprofit() > 0){
			log.info("订单编号:" + order.getOrderNumber() + " , 商户自己获得分润:" + fee.getMerchantprofit());
			obj = new Object[]{getUUID(),loginUser.getID(),fee.getMerchantprofit(), order.getTradeDate() + order.getTradeTime(),order.getID()};
			list.add(obj);
		}
		return list;
	}
	
	
	
	/**
	 *   计算固定码手续费
	 * @param order
	 * @param loginUser
	 * @param map
	 * @return
	 */
	/**
	 *   计算固定码手续费
	 * @param order
	 * @param loginUser
	 * @param map
	 * @return
	 */
	public Fee YMFcalProfit(PayOrder order , TabLoginuser user , Map<String,Object> qrcode){
		
		log.info("计算订单" + order.getOrderNumber() + "固定码手续费");
		
		Fee fee = new Fee();
		
		/** 到账类型 **/
		String tradeCode = order.getTradeCode();
		
		/** 固定码的手续费 **/
		String rate = ObjToStr(qrcode.get("Rate"));
		
		/** 固定码计算费率 ***/
		String SettlementRate = ObjToStr(qrcode.get("SettlementRate"));
		
		/** t0 附加费用 **/
		int T0additional = 0;
		
		/** 代理商信息 **/
		String agentinfosql = "select * from tab_agent where ID=?";
		Map<String,Object> agentInfo = queryForMap(agentinfosql, new Object[]{user.getAgentID()});
		
		/** 代理商费率配置信息 **/
		String agentconfigsql = "select * from tab_agent_config where AgentID=? and PayChannel=?";
		Map<String,Object> agentConfig = queryForMap(agentconfigsql, new Object[]{agentInfo.get("ID") , order.getPayChannel()});
		
		
		boolean flag = false;
		
		Map<String,Object> salemsMan  = null;
		/**  交易商户 的业务员ID **/
		String salemsManID = user.getSalesManID();
		if(salemsManID != null && !salemsManID.trim().isEmpty()){
			flag = true;
			String salesmaninfosql = "select * from tab_salesman where ID=?";
			salemsMan = queryForMap(salesmaninfosql, new Object[]{salemsManID});
			if(salemsMan == null || salemsMan.isEmpty()){
				flag = false;
			}else{
				fee.setSalemsManID(salemsMan.get("ID").toString()); 
				log.info("计算订单：" + order.getOrderNumber() + " 包含业务员  ， 业务员ID：" + salemsMan.get("ID"));
			}
 		}
		
		
		/**   代理商签约成本 ,  渠道成本  **/
		String agentRate = "0" , channelRate ="0" , merchantRate = "0";
		
		/**  业务员分润费率 **/
		String salesManRate = "0";
		
		/** 交易类型为T0 **/
		if(tradeCode.equals("T0")){
			
			log.info("订单:" + order.getOrderNumber() + "为T0交易"); 
			
			//  T0代理商成本
			agentRate = ObjToStr(agentConfig.get("T0AgentRate"));
			//  T0渠道成本
			channelRate = ObjToStr(agentConfig.get("T0ChannelRate"));
			
			merchantRate = ObjToStr(agentConfig.get("T0MerchantRate"));
			
			//  T0附加手续费
			String t0additionalsql = "select * from tab_appconfig";
			T0additional = Integer.parseInt(ObjToStr(queryForMap(t0additionalsql, null).get("T0AttachFee")));
			
			if(flag){
				
				//  存在业务员信息
				salesManRate = salemsMan.get("T0FeeRate").toString();
				
				log.info("业务员费率值：" + salesManRate );
			}
			
			
			log.info(order.getOrderNumber() + "交易费率为：" + rate + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}else{
			
			log.info("订单:" + order.getOrderNumber() + "为T1交易"); 
			
			/** 交易类型为T1 **/
			// 代理商成本费率
			agentRate = ObjToStr(agentConfig.get("AgentRate"));
			//  渠道成本
			channelRate = ObjToStr(agentConfig.get("ChannelRate"));
			
			//  商户下放费率
			merchantRate = ObjToStr(agentConfig.get("MerchantRate"));
			
			
			if(flag){
				
				//  存在业务员信息
				salesManRate = salemsMan.get("FeeRate").toString();
				
				log.info("业务员费率值：" + salesManRate );
			}
			
			log.info(order.getOrderNumber()  + "交易费率为：" + rate + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
		}
		
		/** 商户手续费 **/
		fee.setMerchantFee(makeFeeRounding(order.getAmount(), Double.valueOf(rate) , 0) + T0additional);
		
		/** 商户自己的分润  **/
		fee.setMerchantprofit(makeFeeAbandon(order.getAmount(),AmountUtil.sub(rate, SettlementRate), 0));
		/** 三级分销总金额  **/
		int distributeProfit = makeFeeAbandon(order.getAmount(),AmountUtil.sub(SettlementRate, merchantRate), 0);
		fee.setDistributeProfit(distributeProfit);
		
		if("1".equals(ObjToStr(qrcode.get("AgentProfit")))){
			// 计算代理商分润
			/** 二级代理商商户交易 **/
			if(agentInfo.get("ParentAgentID")!=null&&!agentInfo.get("ParentAgentID").equals(agentInfo.get("ID"))){
				
				log.info(order.getOrderNumber() + "订单的商户的代理商为二级代理商");
				
				//  获得父级代理商ID
				String agentParentsql = "select * from tab_agent where ID=?";
				Map<String,Object> agentParent =queryForMap(agentParentsql , new Object[]{agentInfo.get("ParentAgentID")});
				
				//  获取父类代理商配置信息
				String agentParentConfigsql = "select * from tab_agent_config where AgentID=? and PayChannel=?";
				Map<String,Object> agentParentConfig = queryForMap(agentParentConfigsql , new Object[]{agentParent.get("ID") , order.getPayChannel()});
				
				//  代理商ID
				fee.setAgentID(agentParent.get("ID").toString());
				//  二级代理商ID
				fee.setTwoAgentID(agentInfo.get("ID").toString());
				
				String parentAgentRate = "0";
				
				if(tradeCode.equals("T0")){
					parentAgentRate = agentParentConfig.get("T0AgentRate").toString();
				}else{
					parentAgentRate = agentParentConfig.get("AgentRate").toString();
				}
				
				if(flag){
					//  如果存在业务员
					int salemsGetAgentProfit = makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,salesManRate) ,0);
					
					log.info("订单："+order.getOrderNumber() + "从代理商获取的收益为： " + salemsGetAgentProfit);
					fee.setSalemsGetAgentProfit(salemsGetAgentProfit);
					merchantRate = salesManRate;
				}
				
				
				//  代理商分润
				fee.setAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(agentRate ,parentAgentRate ) ,0));
				//  设置二级代理商分润
				fee.setTwoAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate), 0));
				
				agentRate = parentAgentRate;
			}else{
				log.info(order.getOrderNumber() + "订单商户的代理商为一级代理商");
				// 代理商ID
				fee.setAgentID(agentInfo.get("ID").toString());
				
				if(flag){
					//  如果存在业务员
					int salemsGetAgentProfit = makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,salesManRate) ,0);
					fee.setSalemsGetAgentProfit(salemsGetAgentProfit);
					
					log.info("订单："+order.getOrderNumber() + "从代理商获取的收益为： " + salemsGetAgentProfit);
					
					merchantRate = salesManRate;
				}
				
				System.out.println("agentRate：" + agentRate);
				
				//  代理商分润
				fee.setAgentProfit(makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate) ,0));
			} 
		} else {
			log.info(order.getOrderNumber() + "不计算代理商分润, 使用的固定码为：" + ObjToStr(qrcode.get("code"))); 
		}
		
		//计算平台手续费
		fee.setPlatCostFee(makeFeeFurther(order.getAmount(), Double.valueOf(channelRate),0));
		// 平台收益
		fee.setPlatformProfit(makeFeeFurther(order.getAmount(),  AmountUtil.sub(agentRate, channelRate),0));
		
		return fee;
	} 
	
	
	public String getUUID(){
		return UUID.randomUUID().toString().toUpperCase();
	}
	
	/**
	 *  判断obj对象是否为空 如果不为null 将返回对应的字符
	 * @param obj
	 * @return
	 */
	public String ObjToStr(Object obj){
		if(obj==null){
			return "";
		}
		return obj.toString();
	}
	
	
	/**
	 *   全部舍掉
	 * @param amount
	 * @param fee
	 * @param top
	 * @return
	 */
	public int makeFeeAbandon(String amount, Double fee, int top) {
		DecimalFormat format = new DecimalFormat("0.00");
		fee = Double.parseDouble(format.format(fee));
		Double feeTemp = (Long.parseLong(amount) * fee) / 1000;
		int poundage = (new Double(feeTemp)).intValue();
		if (poundage > top && top != 0) {
			return top;
		} else {
			return poundage;
		}
	}

	/**
	 *   舍弃小数部分并进一位
	 * @param amount
	 * @param fee
	 * @param top
	 * @return
	 */
	public int makeFeeFurther(String amount, Double fee, int top) {
		Double feeTemp = Math.ceil((Long.parseLong(amount) * fee) / 1000);
		int poundage = (new Double(feeTemp)).intValue();
		if (poundage > top && top != 0) {
			return top;
		} else {
			return poundage;
		}
	}
	
	
	/**
	 *   四舍五入 保留整数
	 * @param amount
	 * @param fee
	 * @param top
	 * @return
	 */
	public int makeFeeRounding(String amount, Double fee, int top) {
		DecimalFormat format = new DecimalFormat("0.00");
		fee = Double.parseDouble(format.format(fee));
		Double feeTemp = (Long.parseLong(amount) * fee) / 1000;
		int poundage = (int) Math.round(feeTemp);
		if (poundage > top && top != 0) {
			return top;
		} else {
			return poundage;
		}
	}
	
	/**
	 *   判断字符串是否为空
	 * @param str
	 * @return
	 */
	public boolean strIsEmpty(String str){
		if(str==null||str.trim().isEmpty()){
			return true;
		}
		return false;
	}
}
