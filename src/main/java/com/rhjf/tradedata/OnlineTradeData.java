package com.rhjf.tradedata;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.MailBean;
import com.rhjf.email.SendMail;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *   新平台每天交易信息
 * @author a
 *
 */
public class OnlineTradeData extends OnlineBaseDao{

	
	Logger log = Logger.getLogger(this.getClass());
	
	public void init () throws IOException{ 
		log.info("开始统计前一天交易数据");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		
		log.info("获取前一天的日期:" + date);
		
		String sql = "select c.merchantNo,c.merchantName,c.agentName,c.count,ifnull(a.SUCCESS , 0) as success , "
				+ "ifnull(b.fail , 0) as fail , ifnull(a.successsumamount , 0) as successsumamount , ifnull(a.SUCCESSmaxamount , 0) as SUCCESSmaxamount ,"
				+ "ifnull(a.SUCCESSavgamount,0) as SUCCESSavgamount , ifnull(b.failsumamount,0) as failsumamount , "
				+ "ifnull( b.failmaxamount, 0) as failmaxamount , ifnull(b.failavgamount,0) as failavgamount , ifnull(success/count , 0) as successbfb , ifnull(fail/count , 0) as failbfb  from "
				+ "(select t.merchantNo , merchant.signName as merchantName , agent.signName as agentName , count(1) as count"
				+ " from tbl_online_order as t INNER join tbl_pay_merchant as merchant INNER JOIN tbl_pay_agent as agent"
				+ " on t.merchantId=merchant.id and t.merchantNo=merchant.merchantNo and merchant.agent_id=agent.id "
				+ " where left(t.createDate , 10)='" + date + "'  GROUP BY t.merchantNo) as c "
				+ "LEFT JOIN "
				+ "(select t.merchantNo , merchant.signName as merchantName , agent.signName as agentName , count(1) as SUCCESS , sum(orderAmount) successsumamount  ,"
				+ " MAX(orderAmount) as  SUCCESSmaxamount , avg(orderAmount) as SUCCESSavgamount from tbl_online_order as t "
				+ "INNER join tbl_pay_merchant as merchant INNER JOIN tbl_pay_agent as agent"
				+ " on t.merchantId=merchant.id and t.merchantNo=merchant.merchantNo and merchant.agent_id=agent.id "
				+ " where left(t.createDate , 10)='" + date + "' and t.orderStatus='SUCCESS' GROUP BY t.merchantNo) as a "
				+ " on c.merchantNo=a.merchantNo "
				+ "left JOIN "
				+ "(select t.merchantNo , merchant.signName as merchantName , agent.signName as agentName , count(1) as fail , "
				+ "sum(orderAmount) failsumamount,MAX(orderAmount) as failmaxamount , avg(orderAmount) as failavgamount from tbl_online_order as t"
				+ " INNER join tbl_pay_merchant as merchant INNER JOIN tbl_pay_agent as agent"
				+ " on t.merchantId=merchant.id and t.merchantNo=merchant.merchantNo and merchant.agent_id=agent.id "
				+ " where left(t.createDate , 10)='" + date + "' and t.orderStatus!='SUCCESS' GROUP BY t.merchantNo )"
				+ " as b on c.merchantNo=b.merchantNo";
		
		log.info("执行的sql：" + sql);
		List<Map<String,Object>> list = queryForList(sql ,  null);
		createExcel(list , date);
	}
	
	public void channelMerchantTradeData() throws IOException{ 
		log.info("开始统计前一天上游通道商户数据");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		
		String sql = "select a.superMerchantNo,a.shopName,a.count,a.bankId,ifnull(successcount,0) as successcount,ifnull(successAmount,0) as successAmoutn , "
				+ "ifnull(successMaxAmount,0) as successMaxAmount ,ifnull(successAvgAmount,0) as successAvgAmount , ifnull(failcount,0) as failcount ,"
				+ "ifnull(failAmount,0) as failAmount  , ifnull(failMaxAmount,0) as failMaxAmount , ifnull(failAvgAmount,0) as failAvgAmount ,"
				+ "ifnull(successcount/count,0) as cgbfb , ifnull(failcount/count,0) as sbbfb from "
				+ "(select  tpm.superMerchantNo  , tpm.shopName ,bankId, count(1) as count "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchantchannelconfig as tpm on too.superMerchantNo=tpm.superMerchantNo "
				+ "where left(too.createDate,10)='"+ date +"' GROUP BY superMerchantNo , bankId ) as a"
				+ " LEFT JOIN "
				+ "(select tpm.superMerchantNo , tpm.shopName ,bankId, count(1) as successcount , sum(orderAmount) as successAmount ,"
				+ " max(orderAmount) as successMaxAmount , avg(orderAmount) as successAvgAmount "
				+ "from tbl_online_order as too  INNER JOIN tbl_pay_merchantchannelconfig as tpm on too.superMerchantNo=tpm.superMerchantNo "
				+ "where left(too.createDate,10)='" + date +"' and orderStatus='SUCCESS' GROUP BY superMerchantNo , bankId  ) as  b "
				+ "on a.superMerchantNo=b.superMerchantNo "
				+ "left join "
				+ "(select tpm.superMerchantNo , tpm.shopName ,bankId, count(1) as failcount , sum(orderAmount) as failAmount , "
				+ "max(orderAmount) as failMaxAmount  , avg(orderAmount) as failAvgAmount "
				+ " from tbl_online_order as too  INNER JOIN tbl_pay_merchantchannelconfig as tpm on too.superMerchantNo=tpm.superMerchantNo"
				+ " where left(too.createDate,10)='" + date +"' and orderStatus!='SUCCESS' GROUP BY superMerchantNo , bankId ) as c"
				+ " on a.superMerchantNo=c.superMerchantNo";
		
		log.info("通道商户执行的sql：" + sql);
		List<Map<String,Object>> list = queryForList(sql ,  null);
		createExcel2(list, date);
	}
	
	
	public void agentTradeData()throws IOException{ 
		log.info("统计前一天代理商交易数据");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		String sql =  "select id , signName , ifnull(wx.orderAmount , 0) as wxorderAmount , ifnull(wx.count , 0) as wxcount , ifnull(wx.avgAmount , 0) as wxavgAmount , ifnull(wx.percent , 0) as wxpercent , "
				+ "ifnull(alipay.orderAmount , 0) as alipayorderAmount , ifnull(alipay.count , 0) as alipaycount , ifnull(alipay.avgAmount , 0) as alipayavgAmount , ifnull(alipay.percent , 0) as alipaypercent , "
				+ "ifnull(kuai.orderAmount , 0) as kuaiorderAmount , ifnull(kuai.count , 0) as kuaicount , ifnull(kuai.avgAmount , 0) as kuaiavgAmount , ifnull(kuai.percent , 0) as kuaipercent , "
				+ "ifnull(UnionPay.orderAmount , 0) as UnionPayorderAmount , ifnull(UnionPay.count , 0) as UnionPaycount , ifnull(UnionPay.avgAmount , 0) as UnionPayavgAmount , ifnull(UnionPay.percent , 0) as UnionPaypercent ,"
				+ "ifnull(NET.orderAmount , 0) as NETorderAmount , ifnull(NET.count , 0) as NETcount , ifnull(NET.avgAmount , 0) as NETavgAmount , ifnull(NET.percent , 0) as NETpercent "
				+ "from "
				+ "(select tpa.id  , tpa.signName  "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id INNER JOIN tbl_pay_agent as tpa on tpm.agent_id=tpa.id  "
				+ "where date(too.createDate)='" + date +"' GROUP BY tpa.id) as tpa "
				+ "LEFT JOIN "
				+ "(select a.agent_id , orderAmount , count , avgAmount , count/totalCount*100 as percent from  "
				+ "(select tpm.agent_id ,  sum(too.orderAmount) as orderAmount , count(1) as count  , round(avg(too.orderAmount) , 2) as avgAmount "   
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.orderStatus='SUCCESS' and too.bankid='WX' GROUP BY tpm.agent_id  ) as a "
				+ "INNER JOIN "
				+ "(select tpm.agent_id , count(1) as totalCount    "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.bankid='WX' GROUP BY tpm.agent_id  ) as b on a.agent_id=b.agent_id ) as wx "
				+ "on tpa.id = wx.agent_id "
				+ "LEFT JOIN "
				+ "(select a.agent_id , orderAmount , count , avgAmount , count/totalCount*100 as percent from "
				+ "(select tpm.agent_id ,  sum(too.orderAmount) as orderAmount , count(1) as count  , round(avg(too.orderAmount) , 2) as avgAmount  " 
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.orderStatus='SUCCESS' and too.bankid='Alipay' GROUP BY tpm.agent_id  ) as a "
				+ "INNER JOIN "
				+ "(select tpm.agent_id , count(1) as totalCount    "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.bankid='Alipay' GROUP BY tpm.agent_id  ) as b on a.agent_id=b.agent_id ) as alipay "
				+ "on tpa.id = alipay.agent_id "
				+ "LEFT JOIN "
				+ "(select a.agent_id , orderAmount , count , avgAmount , count/totalCount*100 as percent from "
				+ "(select tpm.agent_id ,  sum(too.orderAmount) as orderAmount , count(1) as count  , round(avg(too.orderAmount) , 2) as avgAmount  "  
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.orderStatus='SUCCESS' and too.channelId='CJ' GROUP BY tpm.agent_id  ) as a "
				+ "INNER JOIN "
				+ "(select tpm.agent_id , count(1) as totalCount  "  
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.channelId='CJ' GROUP BY tpm.agent_id  ) as b on a.agent_id=b.agent_id ) as kuai "
				+ "on tpa.id = kuai.agent_id "
				+ "LEFT JOIN  "
				+ "(select a.agent_id , orderAmount , count , avgAmount , count/totalCount*100 as percent from  "
				+ "(select tpm.agent_id ,  sum(too.orderAmount) as orderAmount , count(1) as count  , round(avg(too.orderAmount) , 2) as avgAmount  "  
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.orderStatus='SUCCESS' and too.channelId='UNIONPAYQRCODE' GROUP BY tpm.agent_id  ) as a "
				+ "INNER JOIN  "
				+ "(select tpm.agent_id , count(1) as totalCount "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.channelId='UNIONPAYQRCODE' GROUP BY tpm.agent_id  ) as b on a.agent_id=b.agent_id ) as UnionPay "
				+ "on tpa.id = UnionPay.agent_id "
				+ "LEFT JOIN  "
				+ "(select a.agent_id , orderAmount , count , avgAmount , count/totalCount*100 as percent from  "
				+ "(select tpm.agent_id ,  sum(too.orderAmount) as orderAmount , count(1) as count  , round(avg(too.orderAmount) , 2) as avgAmount  "  
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.orderStatus='SUCCESS' and too.b2bOrB2c='NET' GROUP BY tpm.agent_id  ) as a "
				+ "INNER JOIN  "
				+ "(select tpm.agent_id , count(1) as totalCount "
				+ "from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantId=tpm.id "
				+ "where date(too.createDate)='" + date +"' and too.b2bOrB2c='NET' GROUP BY tpm.agent_id  ) as b on a.agent_id=b.agent_id ) as NET "
				+ "on tpa.id = NET.agent_id ";
		log.info("代理商数据执行的sql：" + sql);
		List<Map<String,Object>> list = queryForList(sql ,  null);
		createExcelAgent(list, date);
	}
	
	
	public Map<String,String> agentTotal(String date){
		
		String sql = "select ifnull(amount , 0) as amount , count ,  ifnull(avgamount , 0) as avgamount , count/totalcount*100 as percent from  "
				+ " (select ifnull(sum(orderAmount) , 0) as amount , count(1) as count , ifnull(ROUND(avg(orderAmount),2) , 0) as avgamount ,ifnull(bankId , 'WX' ) as bankId "
				+ " from tbl_online_order where date(createDate) = '"+ date +"'  and bankId='WX' and orderStatus='SUCCESS') as wxa "
				+ " INNER JOIN (select count(1) as totalcount  , bankId  from tbl_online_order where date(createDate) = '"+ date +"' and bankId='WX') as wxb"
				+ " on wxa.bankid=wxb.bankid";
		
		log.info("微信sql ： " + sql);
		Map<String,String> map = new HashMap<String,String>();
		
		Map<String,String> wxmap = queryForList(sql).get(0);
		
		map.put("wxamount", wxmap.get("amount"));
		map.put("wxcount", wxmap.get("count"));
		map.put("wxavgamount", wxmap.get("avgamount"));
		map.put("wxpercent", wxmap.get("percent"));
		
		sql = "select ifnull(amount , 0) as amount , count , ifnull(avgamount , 0) as avgamount , count/totalcount*100 as percent from  "
				+ " (select ifnull(sum(orderAmount) , 0) as amount , count(1) as count , ifnull(ROUND(avg(orderAmount),2) , 0) as avgamount ,ifnull(bankId , 'Alipay' ) as bankId "
				+ " from tbl_online_order where date(createDate) = '"+ date +"'  and bankId='Alipay' and orderStatus='SUCCESS') as wxa "
				+ " INNER JOIN (select count(1) as totalcount  , bankId  from tbl_online_order where date(createDate) = '"+ date +"' and bankId='Alipay') as wxb"
				+ " on wxa.bankid=wxb.bankid";
		
		log.info("支付宝 sql ： " + sql);
		wxmap = queryForList(sql).get(0);
		
		map.put("Aliamount", wxmap.get("amount"));
		map.put("Alicount", wxmap.get("count"));
		map.put("Aliavgamount", wxmap.get("avgamount"));
		map.put("Alipercent", wxmap.get("percent"));
		
		
		sql = "select ifnull(amount , 0) as amount  , count , ifnull(avgamount , 0) as avgamount  , count/totalcount*100 as percent from  "
				+ " (select sum(orderAmount) as amount , count(1) as count , ROUND(avg(orderAmount),2) as avgamount  ,  channelId "
				+ " from tbl_online_order where date(createDate) = '"+ date +"'  and channelId='CJ' and orderStatus='SUCCESS') as wxa "
				+ " INNER JOIN (select count(1) as totalcount  , channelId  from tbl_online_order where date(createDate) = '"+ date +"' and channelId='CJ') as wxb"
				+ " on wxa.channelId=wxb.channelId";
		log.info("快捷支付 sql ： " + sql);
		wxmap = queryForList(sql).get(0);
		
		map.put("KUAIamount", wxmap.get("amount"));
		map.put("KUAIcount", wxmap.get("count"));
		map.put("KUAIavgamount", wxmap.get("avgamount"));
		map.put("KUAIpercent", wxmap.get("percent"));
		
		
		sql = "select ifnull(amount, 0 )  as  amount , ifnull(count , 0) as count  , ifnull(avgamount , 0) as avgamount ,"
				+ " ifnull(count/totalcount*100 , 0) as percent from  "
				+ " (select ifnull(sum(orderAmount) , 0) as amount , count(1) as count , ifnull(ROUND(avg(orderAmount),2) , 0) as avgamount ,ifnull(channelId , 'UNIONPAYQRCODE' ) as channelId "
				+ " from tbl_online_order where date(createDate) = '"+ date +"'  and  channelId='UNIONPAYQRCODE' and orderStatus='SUCCESS') as wxa "
				+ " left JOIN (select count(1) as totalcount  , channelId  from tbl_online_order where date(createDate) = '"+ date +"' and channelId='UNIONPAYQRCODE') as wxb"
				+ " on wxa.channelId=wxb.channelId";
		
		log.info("银联二维码 sql ： " + sql);
		wxmap = queryForList(sql).get(0);
		map.put("UnionPayamount", wxmap.get("amount"));
		map.put("UnionPaycount", wxmap.get("count"));
		map.put("UnionPayavgamount", wxmap.get("avgamount"));
		map.put("UnionPaypercent", wxmap.get("percent"));
		
			
			
		sql = "select ifnull(amount, 0 )  as  amount , ifnull(count , 0) as count  , ifnull(avgamount , 0) as avgamount ,"
				+ " ifnull(count/totalcount*100 , 0) as percent from  "
				+ " (select sum(orderAmount) as amount , count(1) as count , ROUND(avg(orderAmount),2) as avgamount  , b2bOrB2c as bankId "
				+ " from tbl_online_order where date(createDate) = '"+ date +"'  and b2bOrB2c='NET' and orderStatus='SUCCESS') as wxa "
				+ " left JOIN (select count(1) as totalcount  , b2bOrB2c as bankId  from tbl_online_order where date(createDate) = '"+ date +"' and b2bOrB2c='NET') as wxb"
				+ " on wxa.bankid=wxb.bankid";
		
		log.info("网关 sql ： " + sql);	
		wxmap = queryForList(sql).get(0);
		map.put("NETamount", wxmap.get("amount"));
		map.put("NETcount", wxmap.get("count"));
		map.put("NETavgamount", wxmap.get("avgamount"));
		map.put("NETpercent", wxmap.get("percent"));
			
		
		return map;
	}
	
	public static void main(String[] args) throws IOException { 
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE, -1);
		String date = sdf.format(c.getTime());
		
		OnlineTradeData wx = new OnlineTradeData();
		wx.init();
		wx.channelMerchantTradeData();
		wx.agentTradeData();
		
		wx.sendMail(date + "交易统计数据 ", date + "交易统计数据报表 ", new String[]{"/opt/sh/data/" + date +".xls" , "/opt/sh/data/" + date +"channel.xls",
				"/opt/sh/data/" + date +"agent.xls"});
	}
	
	
	public void createExcelAgent(List<Map<String,Object>> list , String date) throws IOException{
		if(!new File("/opt/sh/data/").exists()){
			new File("/opt/sh/data/").mkdirs();
		}
		log.info("查询通道商户交易信息 : " + list.size() + "开始创建Excel文件");
		
		// 创建Excel的工作书册 Workbook,对应到一个excel文档  
	    HSSFWorkbook wb = new HSSFWorkbook();  
	  
	    // 创建Excel的工作sheet,对应到一个excel文档的tab  
	    HSSFSheet sheet = wb.createSheet("交易统计数据");  
	    
	    // 创建Excel的sheet的一行  
	    HSSFRow titl = sheet.createRow(0); 
	    
	    HSSFCell titlcell = titl.createCell(0);  
	    titlcell.setCellValue("日期");
	    
	    titlcell = titl.createCell(1);  
	    titlcell.setCellValue("代理商名称");
	    
	    
	    titlcell =  titl.createCell(2);
	    titlcell.setCellValue("微信");
	    
	    titlcell =  titl.createCell(6);
	    titlcell.setCellValue("支付宝");

	    titlcell =  titl.createCell(10);
	    titlcell.setCellValue("无卡快捷");
	    
	    titlcell =  titl.createCell(14);
	    titlcell.setCellValue("银联二维码");
	    
	    
	    titlcell =  titl.createCell(18);
	    titlcell.setCellValue("网银");
	   
	    CellRangeAddress cra =new CellRangeAddress(0, 0, 2, 5); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
		cra =new CellRangeAddress(0, 0, 6, 9); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
				
		cra =new CellRangeAddress(0, 0, 10, 13); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
		
		cra =new CellRangeAddress(0, 0, 14, 17); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
		
		cra =new CellRangeAddress(0, 0, 18, 21); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
		
		cra =new CellRangeAddress(0, 1, 0, 0); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
		cra =new CellRangeAddress(0, 1, 1, 1); // 起始行, 终止行, 起始列, 终止列
		sheet.addMergedRegion(cra);
	    
		// 创建Excel的sheet的一行  
	    HSSFRow row = sheet.createRow(1);  
	    
	    HSSFCell cell = row.createCell(2);
	    cell.setCellValue("交易金额");
	    cell = row.createCell(3);
	    cell.setCellValue("交易笔数");
	    cell = row.createCell(4);
	    cell.setCellValue("笔均交易额");
	    cell = row.createCell(5);
	    cell.setCellValue("交易成功率");
	    
	    cell = row.createCell(6);
	    cell.setCellValue("交易金额");
	    cell = row.createCell(7);
	    cell.setCellValue("交易笔数");
	    cell = row.createCell(8);
	    cell.setCellValue("笔均交易额");
	    cell = row.createCell(9);
	    cell.setCellValue("交易成功率");
	    
	    cell = row.createCell(10);
	    cell.setCellValue("交易金额");
	    cell = row.createCell(11);
	    cell.setCellValue("交易笔数");
	    cell = row.createCell(12);
	    cell.setCellValue("笔均交易额");
	    cell = row.createCell(13);
	    cell.setCellValue("交易成功率"); 
	    
	    cell = row.createCell(14);
	    cell.setCellValue("交易金额");
	    cell = row.createCell(15);
	    cell.setCellValue("交易笔数");
	    cell = row.createCell(16);
	    cell.setCellValue("笔均交易额");
	    cell = row.createCell(17);
	    cell.setCellValue("交易成功率"); 
	    
	    
	    cell = row.createCell(18);
	    cell.setCellValue("交易金额");
	    cell = row.createCell(19);
	    cell.setCellValue("交易笔数");
	    cell = row.createCell(20);
	    cell.setCellValue("笔均交易额");
	    cell = row.createCell(21);
	    cell.setCellValue("交易成功率"); 
	    
	    
	    // 创建Excel的sheet的一行  
	    row = sheet.createRow(2);  
	    
	    cell = row.createCell(0); 
	    cell.setCellValue(date); 
	    
	    cell = row.createCell(1); 
	    cell.setCellValue("合计");
	    
	    Map<String,String> totalmap = agentTotal(date);
	    
	    cell = row.createCell(2);
	    cell.setCellValue(totalmap.get("wxamount"));
	    cell = row.createCell(3);
	    cell.setCellValue(totalmap.get("wxcount"));
	    cell = row.createCell(4);
	    cell.setCellValue(totalmap.get("wxavgamount"));
	    cell = row.createCell(5);
	    cell.setCellValue(totalmap.get("wxpercent"));
	    
	    cell = row.createCell(6);
	    cell.setCellValue(totalmap.get("Aliamount"));
	    cell = row.createCell(7);
	    cell.setCellValue(totalmap.get("Alicount"));
	    cell = row.createCell(8);
	    cell.setCellValue(totalmap.get("Aliavgamount"));
	    cell = row.createCell(9);
	    cell.setCellValue(totalmap.get("Alipercent"));
	    
	    cell = row.createCell(10);
	    cell.setCellValue(totalmap.get("KUAIamount"));
	    cell = row.createCell(11);
	    cell.setCellValue(totalmap.get("KUAIcount"));
	    cell = row.createCell(12);
	    cell.setCellValue(totalmap.get("KUAIavgamount"));
	    cell = row.createCell(13);
	    cell.setCellValue(totalmap.get("KUAIpercent")); 
	    
	    cell = row.createCell(14);
	    cell.setCellValue(totalmap.get("UnionPayamount"));
	    cell = row.createCell(15);
	    cell.setCellValue(totalmap.get("UnionPaycount"));
	    cell = row.createCell(16);
	    cell.setCellValue(totalmap.get("UnionPayavgamount"));
	    cell = row.createCell(17);
	    cell.setCellValue(totalmap.get("UnionPaypercent")); 
	    
	    cell = row.createCell(18);
	    cell.setCellValue(totalmap.get("NETamount"));
	    cell = row.createCell(19);
	    cell.setCellValue(totalmap.get("NETcount"));
	    cell = row.createCell(20);
	    cell.setCellValue(totalmap.get("NETavgamount"));
	    cell = row.createCell(21);
	    cell.setCellValue(totalmap.get("NETpercent")); 
	    
	    
	    int j = 3;
	    for(int i=0 ; i< list.size() ; i++ ){
	    	
	    	Map<String,Object> map = list.get(i);
	    	
			row = sheet.createRow(j);
			
			
			j = j + 1;
			// 设置单元格的样式格式
			cell = row.createCell(0);
			cell.setCellValue(date); 
			
			
			cell = row.createCell(1);
			cell.setCellValue(map.get("signName").toString());

			cell = row.createCell(2);
			cell.setCellValue(map.get("wxorderAmount").toString());

			cell = row.createCell(3);
			cell.setCellValue(map.get("wxcount").toString());

			cell = row.createCell(4);
			cell.setCellValue(map.get("wxavgAmount").toString());

			cell = row.createCell(5);
			cell.setCellValue(map.get("wxpercent").toString());

			cell = row.createCell(6);
			cell.setCellValue(map.get("alipayorderAmount").toString());

			cell = row.createCell(7);
			cell.setCellValue(map.get("alipaycount").toString());

			cell = row.createCell(8);
			cell.setCellValue(map.get("alipayavgAmount").toString());

			cell = row.createCell(9);
			cell.setCellValue(map.get("alipaypercent").toString());

			cell = row.createCell(10);
			cell.setCellValue(map.get("kuaiorderAmount").toString());

			cell = row.createCell(11);
			cell.setCellValue(map.get("kuaicount").toString());

			cell = row.createCell(12);
			cell.setCellValue(map.get("kuaiavgAmount").toString());
			
			cell = row.createCell(13);
			cell.setCellValue(map.get("kuaipercent").toString());
			
			cell = row.createCell(14);
			cell.setCellValue(map.get("UnionPayorderAmount").toString());
			
			cell = row.createCell(15);
			cell.setCellValue(map.get("UnionPaycount").toString());
			
			cell = row.createCell(16);
			cell.setCellValue(map.get("UnionPayavgAmount").toString());
			
			cell = row.createCell(17);
			cell.setCellValue(map.get("UnionPaypercent").toString());
			
			
			cell = row.createCell(18);
		    cell.setCellValue(map.get("NETorderAmount").toString());
		    cell = row.createCell(19);
		    cell.setCellValue(map.get("NETcount").toString());
		    cell = row.createCell(20);
		    cell.setCellValue(map.get("NETavgAmount").toString());
		    cell = row.createCell(21);
		    cell.setCellValue(map.get("NETpercent").toString()); 
			
			
	    }
	    FileOutputStream os = new FileOutputStream("/opt/sh/data/" + date +"agent.xls");  
	    wb.write(os);  
	    os.close();  
	    wb.close();
	    log.info("生成文件完成"); 
	}
	
	
	public void createExcel2(List<Map<String,Object>> list , String date) throws IOException{
		if(!new File("/opt/sh/data/").exists()){
			new File("/opt/sh/data/").mkdirs();
		}
		log.info("查询通道商户交易信息 : " + list.size() + "开始创建Excel文件");
		
		   // 创建Excel的工作书册 Workbook,对应到一个excel文档  
	    HSSFWorkbook wb = new HSSFWorkbook();  
	  
	    // 创建Excel的工作sheet,对应到一个excel文档的tab  
	    HSSFSheet sheet = wb.createSheet("交易统计数据");  
	  
	  
	    // 创建Excel的sheet的一行  
	    HSSFRow row = sheet.createRow(0);  
	    
	    HSSFCell cell = row.createCell(0);  
	    
	    cell.setCellValue("商户号");
	    cell = row.createCell(1);
	    cell.setCellValue("商户名称");
	    cell = row.createCell(2);
	    cell.setCellValue("交易总数量");
	    cell = row.createCell(3);
	    cell.setCellValue("成功数量");
	    cell = row.createCell(4);
	    cell.setCellValue("成功总金额");
	    cell = row.createCell(5);
	    cell.setCellValue("成功最大金额");
	    cell = row.createCell(6);
	    cell.setCellValue("成功平均金额");
	    cell = row.createCell(7);
	    cell.setCellValue("失败数量");
	    cell = row.createCell(8);
	    cell.setCellValue("失败总金额");
	    cell = row.createCell(9);
	    cell.setCellValue("失败最大金额");
	    cell = row.createCell(10);
	    cell.setCellValue("失败平均金额");
	    cell = row.createCell(11);
	    cell.setCellValue("成功百分比");
	    cell = row.createCell(12);
	    cell.setCellValue("失败百分比"); 
	    
	    for(int i=0 ; i< list.size() ; i++ ){
	    	
	    	Map<String,Object> map = list.get(i);
	    	
			row = sheet.createRow(i + 1);
			// 设置单元格的样式格式
			cell = row.createCell(0);
			cell.setCellValue(map.get("superMerchantNo").toString());

			cell = row.createCell(1);
			cell.setCellValue(map.get("shopName").toString());

			cell = row.createCell(2);
			cell.setCellValue(map.get("count").toString());

			cell = row.createCell(3);
			cell.setCellValue(map.get("successcount").toString());

			cell = row.createCell(4);
			cell.setCellValue(map.get("successAmoutn").toString());

			cell = row.createCell(5);
			cell.setCellValue(map.get("successMaxAmount").toString());

			cell = row.createCell(6);
			cell.setCellValue(map.get("successAvgAmount").toString());

			cell = row.createCell(7);
			cell.setCellValue(map.get("failcount").toString());

			cell = row.createCell(8);
			cell.setCellValue(map.get("failAmount").toString());

			cell = row.createCell(9);
			cell.setCellValue(map.get("failMaxAmount").toString());

			cell = row.createCell(10);
			cell.setCellValue(map.get("failAvgAmount").toString());

			cell = row.createCell(11);
			cell.setCellValue(map.get("cgbfb").toString());
			
			cell = row.createCell(12);
			cell.setCellValue(map.get("sbbfb").toString());
	    }
	    FileOutputStream os = new FileOutputStream("/opt/sh/data/" + date +"channel.xls");  
	    wb.write(os);  
	    os.close();  
	    wb.close();
	    log.info("生成文件完成"); 
	}
	
	
	public void createExcel(List<Map<String,Object>> list , String date) throws IOException{
		if(!new File("/opt/sh/data/").exists()){
			new File("/opt/sh/data/").mkdirs();
		}
		log.info("查询的数据行数 : " + list.size() + "开始创建Excel文件");
		
		   // 创建Excel的工作书册 Workbook,对应到一个excel文档  
	    HSSFWorkbook wb = new HSSFWorkbook();  
	  
	    // 创建Excel的工作sheet,对应到一个excel文档的tab  
	    HSSFSheet sheet = wb.createSheet("交易统计数据");  
	  
	  
	    // 创建Excel的sheet的一行  
	    HSSFRow row = sheet.createRow(0);  
	    
	    HSSFCell cell = row.createCell(0);  
	    
	    cell.setCellValue("商户号");
	    cell = row.createCell(1);
	    cell.setCellValue("商户名称");
	    cell = row.createCell(2);
	    cell.setCellValue("代理商名称");
	    cell = row.createCell(3);
	    cell.setCellValue("交易总数量");
	    cell = row.createCell(4);
	    cell.setCellValue("成功数量");
	    cell = row.createCell(5);
	    cell.setCellValue("失败数量");
	    cell = row.createCell(6);
	    cell.setCellValue("成功总金额");
	    cell = row.createCell(7);
	    cell.setCellValue("成功最大金额");
	    cell = row.createCell(8);
	    cell.setCellValue("成功平均金额");
	    cell = row.createCell(9);
	    cell.setCellValue("失败总金额");
	    cell = row.createCell(10);
	    cell.setCellValue("失败最大金额");
	    cell = row.createCell(11);
	    cell.setCellValue("失败平均金额");
	    cell = row.createCell(12);
	    cell.setCellValue("成功百分比");
	    cell = row.createCell(13);
	    cell.setCellValue("失败百分比"); 
	    
	    for(int i=0 ; i< list.size() ; i++ ){
	    	
	    	Map<String,Object> map = list.get(i);
	    	
			row = sheet.createRow(i + 1);
			// 设置单元格的样式格式
			cell = row.createCell(0);
			cell.setCellValue(map.get("merchantNo").toString());

			cell = row.createCell(1);
			cell.setCellValue(map.get("merchantName").toString());

			cell = row.createCell(2);
			cell.setCellValue(map.get("agentName").toString());

			cell = row.createCell(3);
			cell.setCellValue(map.get("count").toString());

			cell = row.createCell(4);
			cell.setCellValue(map.get("success").toString());

			cell = row.createCell(5);
			cell.setCellValue(map.get("fail").toString());

			cell = row.createCell(6);
			cell.setCellValue(map.get("successsumamount").toString());

			cell = row.createCell(7);
			cell.setCellValue(map.get("SUCCESSmaxamount").toString());

			cell = row.createCell(8);
			cell.setCellValue(map.get("SUCCESSavgamount").toString());

			cell = row.createCell(9);
			cell.setCellValue(map.get("failsumamount").toString());

			cell = row.createCell(10);
			cell.setCellValue(map.get("failmaxamount").toString());

			cell = row.createCell(11);
			cell.setCellValue(map.get("failavgamount").toString());
			
			cell = row.createCell(12);
			cell.setCellValue(map.get("successbfb").toString());
			
			cell = row.createCell(13);
			cell.setCellValue(map.get("failbfb").toString());
	 	    
	    }
	    FileOutputStream os = new FileOutputStream("/opt/sh/data/" + date +".xls");  
	    wb.write(os);  
	    os.close();  
	    wb.close();
	    
	    
	    log.info("生成文件完成"); 
	    
	}
	
	public void sendMail(String title , String context , String[] files){
		
		log.info("开始发送邮件");
		
		MailBean mb = new MailBean();
		// 设置SMTP主机(163)，若用126，则设为：smtp.126.com
		mb.setHost("smtp.qiye.163.com");
		// 设置发件人邮箱的用户名
		mb.setUsername("zhoufangyu@ronghuijinfubj.com");
		// 设置发件人邮箱的密码，需将*号改成正确的密码
		mb.setPassword("siyanlv3@");
		// 设置发件人的邮箱
		mb.setFrom("zhoufangyu@ronghuijinfubj.com");

		// 设置收件人的邮箱
		mb.setTo("liqiuyou@ronghuijinfubj.com");
		mb.setTo("liukeda@ronghuijinfubj.com");

		//  设置抄送人
		mb.setCopyColumn("ronghui@ronghuijinfubj.com");
		mb.setCopyColumn("jishu@ronghuijinfubj.com");
		mb.setCopyColumn("yunying@ronghuijinfubj.com"); 
		
		mb.setSubject(title); // 设置邮件的主题
		mb.setContent(context); // 设置邮件的正文
		
		//  添加附件
		if(files!=null){
			for (String file : files) {
				mb.attachFile(file); 
			}
		}
		
		SendMail sm = new SendMail();
		log.info("正在发送邮件...");

		// 发送邮件
		if (sm.sendMail(mb)){
			log.info("发送成功!");
		}else{
			log.info("发送失败!");
		}

	}
}
