package com.rhjf.t0query;

public class Fee {

	/**  商户手续费 **/
	private int merchantFee;
	/**  商户自己的分润 **/
	private int merchantprofit;
	
	/** 代理商收益  **/
	private int agentProfit;
	/** 代理商签约成本 **/
	private int agentCostFee;
	/** 二级代理商签约成本 **/
	private int twoAgentProfit;
	
	/** 三级分销总金额 **/
	private int distributeProfit;
	
	/** 三级分销一级利润  **/
	private int oneMerchantProfit;
	/** 三级分销二级利润  **/
	private int twoMerchantProfit;
	/** 三级分销 三级利润 **/
	private int threeMerchantProfit;
	
	
	/**  平台手续费 **/
	private int platCostFee;
	/**  平台收益 **/
	private int platformProfit;
	
	/**  一级代理商ID **/
	private String agentID;
	
	/** 二级代理商ID **/
	private String twoAgentID;
	
	
	/**  业务员ID  **/
	private String salemsManID;
	
	/**  业务员三级分销获取的收益 **/
	private int salemsDistrubuteProfit;
	
	/**  业务员从代理商获取的收益 **/
	private int salemsGetAgentProfit;
	

	public int getMerchantprofit() {
		return merchantprofit;
	}
	public void setMerchantprofit(int merchantprofit) {
		this.merchantprofit = merchantprofit;
	}
	public int getMerchantFee() {
		return merchantFee;
	}
	public void setMerchantFee(int merchantFee) {
		this.merchantFee = merchantFee;
	}
	public int getAgentProfit() {
		return agentProfit;
	}
	public void setAgentProfit(int agentProfit) {
		this.agentProfit = agentProfit;
	}
	public int getOneMerchantProfit() {
		return oneMerchantProfit;
	}
	public void setOneMerchantProfit(int oneMerchantProfit) {
		this.oneMerchantProfit = oneMerchantProfit;
	}
	public int getTwoMerchantProfit() {
		return twoMerchantProfit;
	}
	public void setTwoMerchantProfit(int twoMerchantProfit) {
		this.twoMerchantProfit = twoMerchantProfit;
	}
	public int getThreeMerchantProfit() {
		return threeMerchantProfit;
	}
	public void setThreeMerchantProfit(int threeMerchantProfit) {
		this.threeMerchantProfit = threeMerchantProfit;
	}
	public int getPlatCostFee() {
		return platCostFee;
	}
	public void setPlatCostFee(int platCostFee) {
		this.platCostFee = platCostFee;
	}
	public int getAgentCostFee() {
		return agentCostFee;
	}
	public void setAgentCostFee(int agentCostFee) {
		this.agentCostFee = agentCostFee;
	}
	public int getPlatformProfit() {
		return platformProfit;
	}
	public void setPlatformProfit(int platformProfit) {
		this.platformProfit = platformProfit;
	}
	public int getTwoAgentProfit() {
		return twoAgentProfit;
	}
	public void setTwoAgentProfit(int twoAgentProfit) {
		this.twoAgentProfit = twoAgentProfit;
	}
	public int getDistributeProfit() {
		return distributeProfit;
	}
	public void setDistributeProfit(int distributeProfit) {
		this.distributeProfit = distributeProfit;
	}
	public String getAgentID() {
		return agentID;
	}
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}
	public String getTwoAgentID() {
		return twoAgentID;
	}
	public void setTwoAgentID(String twoAgentID) {
		this.twoAgentID = twoAgentID;
	}
	public String getSalemsManID() {
		return salemsManID;
	}
	public void setSalemsManID(String salemsManID) {
		this.salemsManID = salemsManID;
	}
	public int getSalemsDistrubuteProfit() {
		return salemsDistrubuteProfit;
	}
	public void setSalemsDistrubuteProfit(int salemsDistrubuteProfit) {
		this.salemsDistrubuteProfit = salemsDistrubuteProfit;
	}
	public int getSalemsGetAgentProfit() {
		return salemsGetAgentProfit;
	}
	public void setSalemsGetAgentProfit(int salemsGetAgentProfit) {
		this.salemsGetAgentProfit = salemsGetAgentProfit;
	}
	
}
