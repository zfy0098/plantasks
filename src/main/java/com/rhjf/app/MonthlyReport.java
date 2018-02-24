package com.rhjf.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;


/**
 *  爱码付月报交易统计数据
 * @author hadoop
 *
 */
public class MonthlyReport extends BaseDao {

	Logger log = Logger.getLogger(this.getClass());

	public void init() {

		log.info("统计月报数据");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Date date = new Date();
		log.info("当前月份:" + sdf.format(date));
		String month = sdf.format(getLastDate(date));

		log.info("需要统计的月份:" + month);

		String sql = "insert into tab_monthly_report (ID, UserID , `Month` , TotalAmount, Fee , PureProfit , TradeCount ) "
				+ " select UPPER(UUID()) , a.ID , '" + month + "' as  localdata , ifnull(b.amount , 0) as amount  , "
				+ " ifnull(b.fee , 0) as fee , ifnull(b.pureprofit , 0) as pureprofit  , ifnull(b.count,0) as count   "
				+ " from tab_loginuser as a left JOIN (select UserID,SUM(Amount) as amount , sum(fee) as fee ,"
				+ " count(1) as count , sum(amount)-sum(fee) as pureprofit , left(LocalDate,6) as localdata "
				+ " from tab_pay_order where PayRetCode='0000' and left(LocalDate,6)='" + month + "' GROUP BY UserID )  as b "
				+ " on a.ID=b.UserID where a.BankInfoStatus=1 and a.PhotoStatus=1 and LEFT(registerTime,6)<='" + month + "'";
		int x = executeSql(sql, null);
		log.info("将统计数据保存到 tab_monthly_report 中，执行sql： " + sql + "，受影响行数:" + x);  
		
		log.info("此次任务执行完毕");

	}

	private static Date getLastDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}
	
	
	public static void main(String[] args) {
		MonthlyReport month = new MonthlyReport();
		month.init();
	}
}
