package com.rhjf.pay;


import com.rhjf.email.MailBean;
import com.rhjf.email.SendMail;
import com.rhjf.merchant.UsingMerchant;
import com.rhjf.base.BaseDao;

/**
 *    每天定时任务
 *     	恢复通道表当天交易金额为0
 * @author hadoop
 *
 */
public class Amount extends BaseDao{
	
	public void init(){
		
		log.info("开始执行计划任务! 恢复前一天的交易金额为0"); 
		log.info("====================恢复通道交易金额，将交易金额设置为0");
		String sql = "update tab_passageway set TransactionAmount=0";
		int i = executeSql(sql, null);
		log.info("====================通道金额回复完成，受影响行数:" + i);
		if(i == 0){
			sendMail("支付通道金额恢复异常，请手动恢复!!!");
		}
		log.info("----------------------------------------------------");
		log.info("恢复T0交易限制金额");
		sql = "update tab_t0amount_limit set TransactionAmount=0";
		int ref = executeSql(sql, null);
		log.info("T0交易金额恢复完成 受影响行数：" + i );
		if(ref == 0){
			sendMail("T0交易限制金额恢复异常，请手动恢复!!!"); 
		}
		log.info("计划任务执行结束");
	}
	
	
	public void sendMail(String context){
		MailBean mb = new MailBean();
		mb.setHost("smtp.qiye.163.com"); // 设置SMTP主机(163)，若用126，则设为：smtp.126.com
		mb.setUsername("zhoufangyu@ronghuijinfubj.com"); // 设置发件人邮箱的用户名
		mb.setPassword("siyanlv3@"); // 设置发件人邮箱的密码，需将*号改成正确的密码
		mb.setFrom("zhoufangyu@ronghuijinfubj.com"); // 设置发件人的邮箱
		
		mb.setTo("zhangzhiguo@ronghuijinfubj.com");  // 设置收件人的邮箱
		mb.setTo("zhoufangyu@ronghuijinfubj.com");
		
		mb.setCopyColumn("ronghui@ronghuijinfubj.com");	//  设置抄送人
		mb.setCopyColumn("jishu@ronghuijinfubj.com");
		
		mb.setSubject("计划任务执行异常邮件"); // 设置邮件的主题
		mb.setContent(context); // 设置邮件的正文

		SendMail sm = new SendMail();
		log.info("正在发送邮件...");

		if (sm.sendMail(mb)) // 发送邮件
			log.info("发送成功!");
		else
			log.info("发送失败!");
	}
	
	
	
	public static void main(String[] args) {
		
		UsingMerchant usingMerchant = new UsingMerchant();
		usingMerchant.init();
		
		
		Amount amount = new Amount();
		amount.init();
	}
}
