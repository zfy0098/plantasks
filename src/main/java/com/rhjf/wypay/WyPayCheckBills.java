package com.rhjf.wypay;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.SendMail;
import com.rhjf.utils.AmountUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hadoop on 2018/3/9.
 *
 * @author hadoop
 */
public class WyPayCheckBills extends OnlineBaseDao {

    private static final String CHANNELID = "WyPay";

    private String date;

    public WyPayCheckBills(String date) {
        this.date = date;
    }

    public void init() {

        String filePath = "/opt/sh/unionpay/ainong_file/"+date.replace("-" , "")+"/600000000318_010000.txt";

        File file = new File(filePath);
        BufferedReader bufferedReader = null;
        String temp;

        List<Object[]> list = new ArrayList<>(1000);

        String saveReconciliationSql = "insert into tbl_online_reconciliation "
                + " ( optimistic ,bankorderid , trxAmount , trxDate , trxFee , channelName , dateFlag ) "
                + " values (0 , ? , ? , ? , ? , ? , ? ) ";

        try {

            bufferedReader = new BufferedReader(new FileReader(file));
            bufferedReader.readLine();

            while ((temp = bufferedReader.readLine()) != null) {
                String[] data = temp.split("\\|");

                double amount = AmountUtil.div(data[7], "100");
                double fee = AmountUtil.div(data[8], "100");

                Object[] obj = new Object[]{data[1], amount, data[9], fee, CHANNELID, date};
                list.add(obj);

                if (list.size() > 1000) {
                    executeBatchSql(saveReconciliationSql, list);
                    list.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (list.size() > 0) {
            executeBatchSql(saveReconciliationSql, list);
            list.clear();
        }

        String sql = "select max(id)+1 as maxID from tbl_online_reconciliation" ;
        String maxID = queryForMap(sql, null).get("maxID").toString();

        log.info("开始更新序列, 序列值为：" +  maxID);

        sql = "update sequence_online_reconciliation set next_val=? ";
        executeSql(sql, new Object[]{maxID});

    }


    public void checkOrder(){

        String success = "update TBL_ONLINE_COPYORDER  as a ,  tbl_online_reconciliation  as b  " +
                " set a.orderstatus='SUCCESS' , a.serialNumber = a.bankOrderId , a.checkAmount = b.trxAmount ," +
                " a.checkFee = b.trxFee , a.checkScanCodeStatus= 'SUCCESS' ,  a.channelId = b.channelName , a.cardNo=b.cardno ," +
                " a.toibkn=b.cardtype , a.payerName=b.bankName  , a.desciption=b.unno " +
                " where a.bankOrderId=b.bankOrderId and withdrawTime='"+date+"' and a.channelId='"+CHANNELID+"' and  date(b.dateFlag)=date('"+date+"') " +
                " and a.orderStatus='SUCCESS' ";

        log.info("成功sql ：" +  success);
        int ret = executeSql(success , null);
        log.info("对账成功受影响行数：" + ret);


        if(ret < 0){

            log.info("处理成功数据异常  ，  程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" + success , new String[]{"zhoufangyu@ronghuijinfubj.com" ,  "zhangzhiguo@ronghuijinfubj.com"} , null , null );

            System.exit(1);
        }


        String longData = "insert into tbl_online_copyorder  " +
                " (bankOrderId , checkAmount , checkFee , checkScanCodeStatus , checkWithdrawStatus , cost , createDate , orderStatus , channelId ,payerFee ," +
                " receiverFee , orderWithdrawAmount , orderWithdrawFee , t0PayResult, orderAmount , withdrawTime , mode , cardNo  , toibkn  , payerName , desciption )" +
                " select bankOrderId , trxAmount , trxFee , 'LONG' , 'INIT' , 0 , trxDate , 'SUCCESS' ,  '"+CHANNELID+"' , 0 , 0 , 0 , 0 , 0  , 0 , '"+date+"' , 0  , cardno ," +
                " cardtype , bankName , unno"  +
                " from tbl_online_reconciliation  where bankOrderId not in " +
                " ( " +
                " select bankOrderId  from  TBL_ONLINE_COPYORDER  where withdrawTime='"+date+"' and channelid='"+CHANNELID+"' " +
                " ) " +
                " and date(dateFlag)='"+date+"'  and channelName='"+CHANNELID+"' ;";

        log.info("长款sql：" + longData);
        ret = executeSql(longData , null);
        log.info("长款受影响行数：" + ret);

        if(ret < 0){
            log.info("处理长款数据异常 ， 程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" + longData , new String[]{"zhoufangyu@ronghuijinfubj.com" , "zhangzhiguo@ronghuijinfubj.com"} , null , null );
            System.exit(1);
        }

        String shortData = "update tbl_online_copyorder  set checkScanCodeStatus='SHORT'" +
                "  where withdrawTime='"+date+"' and orderStatus='SUCCESS' and checkScanCodeStatus='INIT' and bankOrderId  not in " +
                " (select bankOrderId from tbl_online_reconciliation  where DATE(dateFlag)='"+date+"' )  and channelId='"+CHANNELID+"' ";

        log.info("短款sql： " + shortData);
        ret = executeSql(shortData , null);

        log.info("短款受影响行数：" + ret);

        if(ret < 0){

            log.info("处理长款数据异常  程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" + shortData , new String[]{"zhoufangyu@ronghuijinfubj.com" ,  "zhangzhiguo@ronghuijinfubj.com"} , null , null );
            System.exit(1);
        }

        log.info("更新copy订单表序列");
        String sql = "select max(id)+1 as maxid from tbl_online_copyorder" ;
        String maxid = queryForMap(sql, null).get("maxid").toString();

        log.info("开始更新序列, 序列值为：" +  maxid);

        sql = "update sequence_online_copyorder set next_val=? ";
        executeSql(sql, new Object[]{maxid});
    }


    public static void main(String[] args) {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE , -1);
        String date = sdf.format(c.getTime());

        if(args.length > 0 ){
            date = args[0];
        }

        WyPayCheckBills wyPayCheckBills = new WyPayCheckBills(date);
        wyPayCheckBills.init();
        wyPayCheckBills.checkOrder();
    }
}
