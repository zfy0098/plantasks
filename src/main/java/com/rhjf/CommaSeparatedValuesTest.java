package com.rhjf;

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
public class CommaSeparatedValuesTest extends OnlineBaseDao{


    public CommaSeparatedValuesTest(){

        log.info("将对账数据导出文件发送邮件");


        // 默认总页数
        Integer totalPages;
        // 每页显示的总条数
        Integer pageSize  = 50000;

        // 保存csv文件的路径
        String path = "/opt/zfy/";


        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        String countSQL = "SELECT count(1) as count " +
                " FROM " +
                " tbl_online_order o," +
                " tbl_pay_merchantchannelconfig c" +
                " WHERE" +
                " o.superMerchantNo = c.superMerchantNo" +
                " AND o.channelId = 'UNIONPAYQRCODE'" +
                " AND createDate < '2018-01-21 00:00:00'" +
                " and o.orderStatus='SUCCESS' ";

        // 获取数据总条数
        Integer count = Integer.parseInt(queryForMapStr(countSQL , null).get("count"));

        //  更新总页数值
        totalPages = count%pageSize==0?count/pageSize:count/pageSize+1;


        log.info("日期  数据总条数:" + count + " , 每页显示：" + pageSize + " , 共显示：" + totalPages + "页");


        for(int i = 0 ; i < totalPages ; i++){

            String sql =  "SELECT" +
                    " c.shopId as 商户号," +
                    " c.shopName as 商户名," +
                    " o.orderAmount as 交易金额," +
                    " o.createDate as 创建时间," +
                    " o.channelCompleteDate as 完成时间," +
                    " o.orderStatus as 交易状态" +
                    " FROM " +
                    " tbl_online_order o," +
                    " tbl_pay_merchantchannelconfig c" +
                    " WHERE" +
                    " o.superMerchantNo = c.superMerchantNo" +
                    " AND o.channelId = 'UNIONPAYQRCODE'" +
                    " AND createDate < '2018-01-21 00:00:00'" +
                    " and o.orderStatus='SUCCESS' limit  " + pageSize*i + " , " + pageSize ;

            List<Map<String,Object>> copyOrderList = queryForList(sql , null);

            List<Object[]> list = new ArrayList<>();

            for (int j = 0 ; j < copyOrderList.size() ; j++){

                Map<String,Object> map = copyOrderList.get(j);

                Object[] obj = new Object[]{map.get("商户号") ,map.get("商户名") ,map.get("交易金额") ,map.get("创建时间")
                        ,map.get("完成时间") ,map.get("交易状态")};

                list.add(obj);
            }

            String[] title = {"商户号" , "商户名" , "交易金额" , "创建时间" ,  "完成时间" , "交易状态"};

            try {
                CreateExcle.createExcel2(title , list , path ,   "--" + i );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String zipFileName = "/opt/123123.zip";
        ZipTool.zip(new File(zipFileName), path);

        sendMail("对账文件", "对账文件", new String[]{zipFileName});

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
        mb.setTo("tyt09099@163.com");
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


    public static void main(String[] args){
        new CommaSeparatedValuesTest();
    }
}
