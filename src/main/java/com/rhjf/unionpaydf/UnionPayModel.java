package com.rhjf.unionpaydf;

/**
 * 银联代付model
 * @author Hebing
 * @version 2016年11月5日 下午12:27:54
 */
public class UnionPayModel {

	private String version; 	//版本号 固定填写1.0
	private String txnType; 	//交易类型 取值：12
	private String merId;   	//商户代码 1、已被批准加入银联互联网系统的商户代码
	                         	//2、银联会为每家机构分配两个商户代码，T1、T0各一个。
	private String settType; 	//清算时效标志0表示T+0 1表示T+1 当merId为T1商编时，settType=1、当merId为T0商编时，settType=0。
	private String oldMerid; 	//原消费交易商户号  当T+0业务时，此字段填写消费交易所用商户号
	private String signature;	//签名 填写对报文摘要的签名
	private String signMethod;	//签名方法  固定填写01
	private String txnTime;   	//订单发送时间
	private String accNo;       //收款人账号
	private String backUrl;		//后台通知地址  交易后台返回代付结果时使用
	private String txnAmt;		//交易金额 单位为分
	private String enterpriseNo;//企业编号  代付企业编号5位数
	private String orderId;		//商户订单号
	private String bankNo;		//收款人开户行行号        当“公/私标识”取值为1时出现
	private String BankName;	//收款人银行中文名称
	private String payName;		//收款人名称
	private String ppType;		//公/私标识    0对私
	private String note;		//备注
	private String phone;		//手机号
	
	private String queryId;     //交易查询流水号，查询交易使用。
	

	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getMerId() {
		return merId;
	}
	public void setMerId(String merId) {
		this.merId = merId;
	}
	public String getSettType() {
		return settType;
	}
	public void setSettType(String settType) {
		this.settType = settType;
	}
	public String getOldMerid() {
		return oldMerid;
	}
	public void setOldMerid(String oldMerid) {
		this.oldMerid = oldMerid;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getSignMethod() {
		return signMethod;
	}
	public void setSignMethod(String signMethod) {
		this.signMethod = signMethod;
	}
	public String getTxnTime() {
		return txnTime;
	}
	public void setTxnTime(String txnTime) {
		this.txnTime = txnTime;
	}
	public String getAccNo() {
		return accNo;
	}
	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}
	public String getBackUrl() {
		return backUrl;
	}
	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}
	public String getTxnAmt() {
		return txnAmt;
	}
	public void setTxnAmt(String txnAmt) {
		this.txnAmt = txnAmt;
	}
	public String getEnterpriseNo() {
		return enterpriseNo;
	}
	public void setEnterpriseNo(String enterpriseNo) {
		this.enterpriseNo = enterpriseNo;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getBankName() {
		return BankName;
	}
	public void setBankName(String bankName) {
		BankName = bankName;
	}
	public String getPayName() {
		return payName;
	}
	public void setPayName(String payName) {
		this.payName = payName;
	}
	public String getPpType() {
		return ppType;
	}
	public void setPpType(String ppType) {
		this.ppType = ppType;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	@Override
	public String toString() {
		return "Unionpay [version=" + version + ", txnType=" + txnType + ", merId=" + merId + ", settType=" + settType
				+ ", oldMerid=" + oldMerid + ", signature=" + signature + ", signMethod=" + signMethod + ", txnTime="
				+ txnTime + ", accNo=" + accNo + ", backUrl=" + backUrl + ", txnAmt=" + txnAmt + ", enterpriseNo="
				+ enterpriseNo + ", orderId=" + orderId + ", bankNo=" + bankNo + ", BankName=" + BankName + ", payName="
				+ payName + ", ppType=" + ppType + ", note=" + note + ", phone=" + phone + "]";
	}
	
	
}
