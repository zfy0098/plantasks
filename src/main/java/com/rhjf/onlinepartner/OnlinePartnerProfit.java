package com.rhjf.onlinepartner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.DateUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OnlinePartnerProfit extends BaseDao {

	private Logger log = Logger.getLogger(this.getClass());
	
	
	private String pathName = "/";
	
	private static JSONArray array = new JSONArray();

	static {
		
		JSONObject partner = new JSONObject();
		
		JSONObject partnerRate = new JSONObject();
		
		partnerRate.put("name", "汲广翔");
		partnerRate.put("ratio", 0.8);
		partnerRate.put("wxt0", 2.6);
		partnerRate.put("wxt1", 2.5);
		partnerRate.put("alipayt0", 2.6);
		partnerRate.put("alipayt1", 2.5);
		partnerRate.put("qqt0", 2.6);
		partnerRate.put("qqt1", 2.5);
		partnerRate.put("kuait0", 3);
		partnerRate.put("kuait1", 3);
		
		partner.put("partnerRate", partnerRate);
		
		
		JSONArray agentArray = new JSONArray();
		
		JSONObject json = new JSONObject();
		json.put("id", "197");
		json.put("agentName", "即米（上海）网络科技及有限公司");
		json.put("wxt0", 2.8);
		json.put("wxt1", 2.7);
		json.put("alipayt0", 2.8);
		json.put("alipayt1", 2.7);
		json.put("qqt0", 2.8);
		json.put("qqt1", 2.7);
		json.put("kuait0", 3.3);
		json.put("kuait1", 3.3);
		
		agentArray.add(json);
		partner.put("agentArray", agentArray);
		
		array.add(partner);

		
		partner = new JSONObject();
		
		partnerRate = new JSONObject();
		partnerRate.put("name", "王宁");
		partnerRate.put("ratio", 1);
		partnerRate.put("wxt0", 2.6);
		partnerRate.put("wxt1", 2.5);
		partnerRate.put("alipayt0", 2.6);
		partnerRate.put("alipayt1", 2.5);
		partnerRate.put("qqt0", 2.6);
		partnerRate.put("qqt1", 2.5);
		partnerRate.put("kuait0", 2.7);
		partnerRate.put("kuait1", 2.5);
		
		partner.put("partnerRate", partnerRate);
		
		agentArray = new JSONArray();
		
		json = new JSONObject();
		json.put("id", "208");
		json.put("agentName", "深圳市前海融脉科技有限公司");
		json.put("wxt0", 4);
		json.put("wxt1", 3.8);
		json.put("alipayt0", 4);
		json.put("alipayt1", 3.8);
		json.put("qqt0", 0);
		json.put("qqt1", 0);
		json.put("kuait0", 3);
		json.put("kuait1", 2.8);
		agentArray.add(json);
		partner.put("agentArray", agentArray);
		
		
		
		array.add(partner);

		
		partner = new JSONObject();
		
		partnerRate = new JSONObject();
		partnerRate.put("name", "许秋桃");
		partnerRate.put("ratio", 0.8);
		partnerRate.put("wxt0", 2.6);
		partnerRate.put("wxt1", 2.5);
		partnerRate.put("alipayt0", 2.6);
		partnerRate.put("alipayt1", 2.5);
		partnerRate.put("qqt0", 2.6);
		partnerRate.put("qqt1", 2.5);
		partnerRate.put("kuait0", 2.6);
		partnerRate.put("kuait1", 2.5);
		
		partner.put("partnerRate", partnerRate);
		
		agentArray = new JSONArray();
		
		json = new JSONObject();
		json.put("id", "248");
		json.put("agentName", "深圳市盛开互联网金融顾问有限公司");
		json.put("wxt0", 3);
		json.put("wxt1", 2.8);
		json.put("alipayt0", 3);
		json.put("alipayt1", 2.8);
		json.put("qqt0", 3);
		json.put("qqt1", 2);
		json.put("kuait0", 3);
		json.put("kuait1", 2.8);
		agentArray.add(json);
		partner.put("agentArray", agentArray);
		
		
		
		array.add(partner);
		
		System.out.println(array.toString());
		
	}

	public void init() throws Exception {

		List<List<Object[]>> proftlist = new ArrayList<List<Object[]>>();
		
		List<String> sheetName = new ArrayList<String>();

		
		for (int j = 0; j < array.size(); j++) {
			
			
			JSONObject json = array.getJSONObject(j);
			
			JSONObject  partnerRate = json.getJSONObject("partnerRate");
			
			JSONArray agentArray = json.getJSONArray("agentArray");
			
			
			sheetName.add(partnerRate.getString("name") + "---" + agentArray.getJSONObject(0).getString("agentName"));
			
			List<Object[]> list = new ArrayList<Object[]>();
			
			for (int k = 0; k < agentArray.size(); k++) {
				
				JSONObject agentJSON = agentArray.getJSONObject(k);

				String agentInfoSQL = "select * from tbl_pay_agent where id=? ";
				Map<String , String> agentInfo  =  queryForMapStr(agentInfoSQL, new Object[]{agentJSON.get("id")}); 
				
//				String createDate = agentInfo.get("createDate");
				
				String createDate = "2017-11-01";
				
				String nowDate = DateUtil.getNowTime(DateUtil.yyyy_MM_dd);
				
				List<String> dateList = getBetweenDates(createDate, nowDate);
				
				Double profitAmount = 0d;
				
				for (int i = 0; i < dateList.size(); i++) {
					
					Double radio = partnerRate.getDouble("ratio");
					DecimalFormat df = new DecimalFormat("######0.00"); 
					
					
					String wxProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
							+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+agentJSON.getDouble("wxt0")+"-"+partnerRate.getDouble("wxt0")+")/1000,2) "
							+ " when t0PayResult=0  then ROUND(too.orderAmount*("+agentJSON.getDouble("wxt1")+"-"+partnerRate.getDouble("wxt1")+")/1000,2) end  as profit , bankid "
							+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
							+ " where tpm.agent_id='"+agentInfo.get("id")+"' and date(too.createDate)=date('"+dateList.get(i)+"') and too.orderStatus='SUCCESS' and bankId='WX') as a ";
					
					log.info(wxProfit);
					
					Map<String,String> profitMap = queryForMapStr(wxProfit, null); 
					
					profitAmount = Double.parseDouble(profitMap.get("profit"));
					profitAmount = profitAmount * radio;
					list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , "微信"});
					
					
					String aliPayProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
							+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+agentJSON.getDouble("alipayt0")+"-"+partnerRate.getDouble("alipayt0")+")/1000,2) "
							+ " when t0PayResult=0  then ROUND(too.orderAmount*("+agentJSON.getDouble("alipayt0")+"-"+partnerRate.getDouble("alipayt1")+")/1000,2) end  as profit , bankid "
							+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
							+ " where tpm.agent_id=? and date(too.createDate)=?  and too.orderStatus='SUCCESS' and bankId='Alipay') as a ";
					
					
					profitMap = queryForMapStr(aliPayProfit, new Object[]{agentInfo.get("id") , dateList.get(i) }); 
					
					profitAmount = Double.parseDouble(profitMap.get("profit"));
					profitAmount = profitAmount * radio;
					list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , "支付宝"});
					
					String QQProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
							+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+agentJSON.getDouble("qqt0")+"-"+partnerRate.getDouble("qqt0")+")/1000,2) "
							+ " when t0PayResult=0  then ROUND(too.orderAmount*("+agentJSON.getDouble("qqt1")+"-"+partnerRate.getDouble("qqt1")+")/1000,2) end  as profit , bankid "
							+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
							+ " where tpm.agent_id=? and date(too.createDate)=?  and too.orderStatus='SUCCESS' and bankId='QQ') as a ";
					
					
					profitMap = queryForMapStr(QQProfit, new Object[]{agentInfo.get("id") , dateList.get(i) }); 
					
					profitAmount = Double.parseDouble(profitMap.get("profit"));
					profitAmount = profitAmount * radio;
					list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , "QQ"});
					
					
					String kuaiProfit = "select bankid , ifnull(sum(profit) , 0) as profit from"
							+ " (select too.orderAmount , too.merchantNo , t0PayResult , case WHEN t0PayResult=1 then  ROUND(too.orderAmount*("+agentJSON.getDouble("kuait0")+"-"+partnerRate.getDouble("kuait0")+")/1000,2) "
							+ " when t0PayResult=0  then ROUND(too.orderAmount*("+agentJSON.getDouble("kuait1")+"-"+partnerRate.getDouble("kuait1")+")/1000,2) end  as profit , bankid "
							+ " from tbl_online_order as too INNER JOIN tbl_pay_merchant as tpm on too.merchantNo=tpm.merchantNo"
							+ " where tpm.agent_id=? and date(too.createDate)=? and too.orderStatus='SUCCESS' and bankId='KUAI') as a ";
					
					
					profitMap = queryForMapStr(kuaiProfit, new Object[]{agentInfo.get("id") , dateList.get(i) }); 
					
					profitAmount = Double.parseDouble(profitMap.get("profit"));
					profitAmount = profitAmount * radio;
					list.add(new Object[]{dateList.get(i) ,df.format(profitAmount) , "无卡快捷"});
					
					profitAmount = 0d;
				}
			}
			proftlist.add(list);
		}
		String[] title = {"日期" , "分润金额" , "交易类型"};
		createExcel2(title, proftlist, sheetName ,pathName, UUID.randomUUID().toString().toUpperCase());
	}

	
	public static List<String> getBetweenDates(String startTime, String endTime) throws Exception {

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
		OnlinePartnerProfit onlinePartnerProfit = new OnlinePartnerProfit();
		onlinePartnerProfit.init();
	}
}
