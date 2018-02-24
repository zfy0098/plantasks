package com.rhjf.onlinepartner;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.MailBean;
import com.rhjf.email.SendMail;
import com.rhjf.utils.*;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  @author hadoop
 */
public class OnlinePartnerProfitNew extends OnlineBaseDao {

	private Logger log = Logger.getLogger(this.getClass());
	
	public void init() throws Exception {

		// 获取以前的日期
		String selectTime = DateUtil.getDateAgo(DateUtil.getNowTime(DateUtil.yyyy_MM_dd), 1, DateUtil.yyyy_MM_dd);

		// 保存excel 文件路径
		String pathName = "/opt/sh/hehuoren/" + selectTime + "/";

		List<List<Object[]>> proftlist = new ArrayList<List<Object[]>>();

		// 每个excel 文件的 sheet 页， 每个sheet 对应每个合伙人的 一个代理商 每个excel 文件对应一个合伙人
		List<String> sheetName = new ArrayList<String>();

		// 获取合伙人集合  and  partnerName='贵州霸王商贸有限公司'
		String sql = "select * from z_partner where active=1   and agentNO='8934938956' and  bankid='KUAI2B' GROUP BY partnerName ";
		List<Map<String,Object>> partnerList = queryForList(sql ,  null);
		
		String fileName = ""; 

		// 遍历合伙人集合
		for (int j = 0; j < partnerList.size(); j++) {
			
			Map<String,Object> map = partnerList.get(j);
			
			Partner p = UtilsConstant.mapToBean(map, Partner.class);

			//  合伙人的名字转成拼音 作为excel 文件的文件名
			fileName = Pinyin.getPingYin(p.getPartnerName());


			//  获取 合伙人下面代理商的集合
			String agentListSQL = "select * from z_partner where active=1 and partnerName=?  GROUP  by agentid ";
			List<Map<String, Object>> agentList = queryForList(agentListSQL , new Object[]{p.getPartnerName()});



			// 遍历代理商信息
			for (int z = 0; z < agentList.size(); z++) {

				p = UtilsConstant.mapToBean(agentList.get(z) , Partner.class);

				sheetName.add(p.getPartnerName() + "---" + p.getAgentName());

				// 存放 sheet页数据
				List<Object[]> list = new ArrayList<Object[]>();

				selectTime = "2017-12-01";
				String endDay = "2017-12-25";

				List<String> dateList = getBetweenDates(selectTime,endDay);

				// 获取代理商交易类型集合
				String bankidsql = "select * from z_partner where partnerName = ? and agentid = ? and active=1";
				List<Map<String,Object>> bankidlist = queryForList(bankidsql, new Object[]{p.getPartnerName() , p.getAgentid()});

				// 遍历代理商交易类型
				for (int k = 0; k < bankidlist.size(); k++) {

					Partner partner = UtilsConstant.mapToBean(bankidlist.get(k) , Partner.class);

					String trade = "无卡快捷";
					String proid = "2";

					if("WX".equals(partner.getBankID())){
						trade = "微信";
						proid = "4";
					}else if("Alipay".equals(partner.getBankID())){
						trade = "支付宝";
						proid = "5";

					}else if("UnionPay".equals(partner.getBankID())){
						trade = "银联二维码";
						proid = "7";

					}else if("NET".equals(partner.getBankID())){
						trade = "网关";
						proid = "1";

					}else if("QQ".equals(partner.getBankID())){
						trade = "QQ支付";
						proid = "6";
					}

					//  and agentFeeType='ACTIVE'
					String agentFeeSQL = "select * from tbl_pay_agent_fee where agentId='"+partner.getAgentid()+"' and product_id='"+proid+"' ";
					Map<String,Object> agentFee = queryForMap(agentFeeSQL , null);

					if(agentFee == null){
						list.add(new Object[]{"" ,"无代理商费率" , trade , "T0"});
						continue;
					}

					log.info(agentFeeSQL);

					partner.setAgentT1rate(String.valueOf(AmountUtil.mul(agentFee.get("t1Fee").toString() , "1000")));
					partner.setAgentT0rate(String.valueOf(AmountUtil.mul(agentFee.get("t0Fee").toString() , "1000")));


					for (int i = 0; i < dateList.size(); i++) {

						Double profitAmount = 0d;
						DecimalFormat df = new DecimalFormat("######0.00");

						String wxProfit;
						Map<String,String> profitMap;
						if(!"0".equals(partner.getT1rate())){
							if("NET".equals(partner.getBankID())){
								wxProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
										+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+new BigDecimal(partner.getAgentT0rate()).subtract(new BigDecimal(partner.getT0rate()))+")/1000,2) "
										+ " when t0PayResult=0  then ROUND(too.orderAmount*("+new BigDecimal(partner.getAgentT1rate()).subtract(new BigDecimal(partner.getT1rate()))+")/1000,2) end  as profit , bankid "
										+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
										+ " where tpm.agent_id='"+ partner.getAgentid() +"' and date(tpm.createDate)<=date('"+dateList.get(i)+"') and date(too.createDate)=date('"+dateList.get(i)+"') and too.orderStatus='SUCCESS' and t0PayResult=0 and b2bOrB2c='NET') as a ";
							}else{
								wxProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
										+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("
										+ new BigDecimal(partner.getAgentT0rate()).subtract(new BigDecimal(partner.getT0rate())) +")/1000,2) "
										+ " when t0PayResult=0  then ROUND(too.orderAmount*("+ new BigDecimal(partner.getAgentT1rate()).subtract(new BigDecimal(partner.getT1rate())) +")/1000,2) end  as profit , bankid "
										+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
										+ " where tpm.agent_id='"+ partner.getAgentid() +"' and date(tpm.createDate)<=date('"+dateList.get(i)+"') and date(too.createDate)=date('"+dateList.get(i)+"') and too.orderStatus='SUCCESS' and t0PayResult=0 and bankId='"+partner.getBankID()+"') as a ";
							}

							profitMap = queryForMapStr(wxProfit, null);

							profitAmount = AmountUtil.mul(UtilsConstant.strIsEmpty(profitMap.get("profit"))?"1":profitMap.get("profit").toString(), partner.getRatio());

							list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , trade , "T1"});
						}



						if(!"0".equals(partner.getT0rate())){
							if("NET".equals(partner.getBankID())){
								wxProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
										+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+new BigDecimal(partner.getAgentT0rate()).subtract(new BigDecimal(partner.getT0rate()))+")/1000,2) "
										+ " when t0PayResult=0  then ROUND(too.orderAmount*("+new BigDecimal(partner.getAgentT1rate()).subtract(new BigDecimal(partner.getT1rate()))+")/1000,2) end  as profit , bankid "
										+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
										+ " where tpm.agent_id='"+ partner.getAgentid() +"' and date(tpm.createDate)<=date('"+dateList.get(i)+"') and date(too.createDate)=date('"+dateList.get(i)+"') and too.orderStatus='SUCCESS' and t0PayResult=1 and b2bOrB2c='NET') as a ";
							}else{
								wxProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
										+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("
										+ new BigDecimal(partner.getAgentT0rate()).subtract(new BigDecimal(partner.getT0rate())) +")/1000,2) "
										+ " when t0PayResult=0  then ROUND(too.orderAmount*("+ new BigDecimal(partner.getAgentT1rate()).subtract(new BigDecimal(partner.getT1rate())) +")/1000,2) end  as profit , bankid "
										+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
										+ " where tpm.agent_id='"+ partner.getAgentid() +"' and date(tpm.createDate)<=date('"+dateList.get(i)+"') and date(too.createDate)=date('"+dateList.get(i)+"') and too.orderStatus='SUCCESS' and t0PayResult=1 and bankId='"+partner.getBankID()+"') as a ";
							}

							log.info(wxProfit);

							profitMap = queryForMapStr(wxProfit, null);

							profitAmount = AmountUtil.mul(UtilsConstant.strIsEmpty(profitMap.get("profit"))?"1":profitMap.get("profit").toString(), partner.getRatio());

							list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , trade , "T0"});
						}
					}
				}
				proftlist.add(list);
				
			}
			String[] title = {"日期" , "分润金额" , "交易类型" , "结算周期"};
			createExcel2(title, proftlist, sheetName , pathName , fileName);
			sheetName.clear();
			proftlist.clear();
		}

		String zipFileName = "/opt/sh/hehuoren/" + selectTime + ".zip";
		ZipTool.zip(new File(zipFileName), pathName);
		sendMail("合伙人分润", "合伙人分润", new String[]{zipFileName});
		
	}

	
	public void sendMail(String title , String context , String[] files){
		
		log.info("开始发送邮件");
		
		MailBean mb = new MailBean();
		mb.setHost("smtp.qiye.163.com"); // 设置SMTP主机(163)，若用126，则设为：smtp.126.com
		mb.setUsername("zhoufangyu@ronghuijinfubj.com"); // 设置发件人邮箱的用户名
		mb.setPassword("siyanlv3@"); // 设置发件人邮箱的密码，需将*号改成正确的密码
		mb.setFrom("zhoufangyu@ronghuijinfubj.com"); // 设置发件人的邮箱

		mb.setTo("qingsuan@ronghuijinfubj.com");
		mb.setTo("zhangzhiguo@ronghuijinfubj.com");  // 设置收件人的邮箱
		mb.setTo("zhoufangyu@ronghuijinfubj.com");
		
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
	
	
	public List<String> getBetweenDates(String startTime, String endTime) throws Exception {

		List<String> dataList = new ArrayList<>();
		dataList.add(startTime.substring(0,10));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dBegin = sdf.parse(startTime);
		Date dEnd = sdf.parse(endTime);

		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calBegin.setTime(dBegin);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calEnd.setTime(dEnd);
		// 测试此日期是否在指定日期之后
		while (dEnd.after(calBegin.getTime())) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			dataList.add(sdf.format(calBegin.getTime()));
		}
		return dataList;
	}
	
	
	public void createExcel2( String[] title , List<List<Object[]>> list  , List<String> sheetName ,String pathName   , String  fileName) throws IOException{
		if(!new File(pathName).exists()){
			new File(pathName).mkdirs();
		}
		// 创建Excel的工作书册 Workbook,对应到一个excel文档  
	    HSSFWorkbook wb = new HSSFWorkbook();  
	  
	    for (int j = 0; j < list.size(); j++) {
	    	  // 创建Excel的工作sheet,对应到一个excel文档的tab  
		    HSSFSheet sheet = wb.createSheet(sheetName.get(j));  
		  
		    // 创建Excel的sheet的一行  
		    HSSFRow row = sheet.createRow(0);  
		    
		    HSSFCell cell = row.createCell(0); 
		    cell.setCellValue(title[0]);
	    	
		    /** 设置标题 **/
		    for (int i = 1; i < title.length; i++) {
			    cell = row.createCell(i);
			    cell.setCellValue(title[i]);
			}
		    
			for (int i = 0; i < list.get(j).size(); i++) {
				row = sheet.createRow(i + 1);
				List<Object[]> l = list.get(j);
				for (int k = 0; k < l.get(i).length; k++) {
					cell = row.createCell(k);
					cell.setCellValue(l.get(i)[k] == null ? "" : l.get(i)[k].toString());
				}
			}
		}
	    
	    FileOutputStream os = new FileOutputStream(pathName + fileName +".xls");  
	    wb.write(os);  
	    os.close();  
	    wb.close();
	}
	
	public static void main(String[] args) throws Exception {
		OnlinePartnerProfitNew onlinePartnerProfit = new OnlinePartnerProfitNew();
		onlinePartnerProfit.init();
	}
}
