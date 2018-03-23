package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.MailBean;
import com.rhjf.email.SendMail;
import com.rhjf.utils.CreateExcle;
import com.rhjf.utils.ZipTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 *  将copyOrder 表中的数据 导成文件 发送邮件
 *
 * Created by hadoop on 2018/2/5.
 *
 * @author hadoop
 */
public class CheckBillsCommaSeparatedValues extends OnlineBaseDao{


    public CheckBillsCommaSeparatedValues(String checkDate){

        log.info("将对账数据导出文件发送邮件");


        // 默认总页数
        Integer totalPages;
        // 每页显示的总条数
        Integer pageSize  = 50000;

        // 保存csv文件的路径
        String path = "/opt/sh/unionpay/csv/" + checkDate.replace("-" , "") + "/";


        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        String countSQL = "SELECT ifnull(count(1) , 0 ) as count  FROM tbl_online_copyorder n " +
                " LEFT JOIN tbl_pay_merchant m ON n.merchantNo = m.merchantNo " +
                " LEFT JOIN tbl_pay_agent a ON m.agent_id = a.id " +
                " LEFT JOIN tbl_pay_merchantsalesmanrelation r ON r.agent_id = a.id " +
                " LEFT JOIN tbl_pay_salesman s ON s.id = r.salesman_id " +
                " WHERE 1 = 1 AND n.orderStatus = 'SUCCESS' AND n.withdrawTime = '"+checkDate+"' ";

        // 获取数据总条数
        Integer count = Integer.parseInt(queryForMapStr(countSQL , null).get("count"));

        //  更新总页数值
        totalPages = count%pageSize==0?count/pageSize:count/pageSize+1;

        log.info("日期：" + checkDate + "数据总条数:" + count + " , 每页显示：" + pageSize + " , 共显示：" + totalPages + "页");

        for(int i = 0 ; i < totalPages ; i++){

            String sql = "SELECT" +
                    " n.id as ID, " +
                    " n.createdate AS 订单创建日期, " +
                    " n.merchantid AS 商户ID, " +
                    " n.checkwithdrawstatus AS 提现对账状态, " +
                    " n.orderamount as 对账金额, " +
                    " n.cost as 手续费 , " +
                    " n.receiverfee as 商户手续费, " +
                    " n.channelid as 通道, " +
                    " oct(n.t0PayResult + 0) AS t0ORt1, " +
                    " n.checkscancodestatus AS 对账状态, " +
                    " n.bankorderid as 通道订单号, " +
                    " n.serialnumber as 平台流水号, " +
                    " n.merchantno as 商户号, " +
                    " n.checkamount as 对账单金额, " +
                    " n.checkfee as 对账单手续费 , " +
                    " n.withdrawamount as 提现金额, " +
                    " n.withdrawfee as 提现手续费, " +
                    " n.withdrawstatus as 提现状态, " +
                    " n.ordernumber as 订单号, " +
                    " n.debitorcredit as  代理商提现手续费, " +
                    " m.signname as 商户签约名, " +
                    " a.signname as 代理商签约名, " +
                    " n.withdrawMsg as 提现描述, " +
                    " s.showName as 销售, " +
                    " a.agentDFCost as 代理商提现手续费, " +
                    " n.withdrawno as 提现流水, " +
                    " n.paytype as 交易类型 ," +
                    " n.agentCost as 代理商成本手续费" +
                    " FROM " +
                    " tbl_online_copyorder n " +
                    " LEFT JOIN tbl_pay_merchant m ON n.merchantNo = m.merchantNo " +
                    " LEFT JOIN tbl_pay_agent a ON m.agent_id = a.id " +
                    " LEFT JOIN tbl_pay_merchantsalesmanrelation r ON r.agent_id = a.id " +
                    " LEFT JOIN tbl_pay_salesman s ON s.id = r.salesman_id " +
                    " WHERE 1 = 1 " +
                    " AND n.orderStatus = 'SUCCESS' " +
                    " AND n.withdrawTime = '" +checkDate+ "'  limit  " + pageSize*i + " , " + pageSize;

            List<Map<String,Object>> copyOrderList = queryForList(sql , null);

            List<Object[]> list = new ArrayList<>(50000);

            for (int j = 0 ; j < copyOrderList.size() ; j++){

                Map<String,Object> map = copyOrderList.get(j);

                Object[] obj = new Object[]{map.get("ID") ,map.get("订单创建日期") ,map.get("商户ID") ,map.get("提现对账状态")
                        ,map.get("对账金额") ,map.get("手续费") ,map.get("商户手续费") ,map.get("通道") ,map.get("t0ORt1")
                        ,map.get("对账状态") ,map.get("通道订单号") ,map.get("平台流水号") ,map.get("商户号") ,map.get("对账单金额")
                        ,map.get("对账单手续费") ,map.get("提现金额") ,map.get("提现手续费") ,map.get("提现状态") ,map.get("订单号")
                        ,map.get("代理商提现手续费") , map.get("商户签约名"), map.get("代理商签约名"), map.get("提现描述")
                        ,map.get("销售"), map.get("提现流水") , map.get("交易类型") , map.get("代理商成本手续费")};

                list.add(obj);
            }

            String[] title = {"ID" , "订单创建日期" , "商户ID" , "提现对账状态" ,  "对账金额" , "手续费", "商户手续费" , "通道", "t0ORt1" , "对账状态" , "通道订单号", "平台流水号"
                    , "商户号", "对账单金额" , "对账单手续费" , "提现金额" , "提现手续费" , "体现状态" , "订单号" , "代理商提现手续费" , "商户签约名" , "代理商签约名"
                    , "提现描述" , "销售" , "提现流水", "交易类型" , "代理商成本手续费"};

            try {
                CreateExcle.createExcel2(title , list , path , checkDate + "--" + i );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String zipFileName = "/opt/sh/unionpay/csv/" + checkDate + ".zip";
        ZipTool.zip(new File(zipFileName), path);

        sendMail("对账文件", "对账文件", new String[]{zipFileName});

    }

    public  void sendMail(String title , String context , String[] files){
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
        mb.setTo("qingsuan@ronghuijinfubj.com");
        mb.setTo("zhangzhiguo@ronghuijinfubj.com");
        mb.setTo("zhoufangyu@ronghuijinfubj.com");


        // 设置邮件的主题
        mb.setSubject(title);

        // 设置邮件的正文
        mb.setContent(context);

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
