package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.SendMail;
import com.rhjf.utils.MrAzuSplitUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/1/9.
 *
 * @author hadoop
 */
public class CheckBills extends OnlineBaseDao {


    private static final String CHANNEL_ID = "UNIONPAYQRCODE";


    private String date ;

    public CheckBills(String date){
        this.date = date;
    }

    public void init() {


        String fileDate = date.replace("-" , "").substring(2);


        String filePath = "/opt/sh/unionpay/duizhang_file/RD2007" +fileDate + "00";


        log.info("获取 yyMMdd 格式的时间:" + fileDate);
        log.info("获取文件路径：" + filePath);
        log.info("开始执行银联二维码对账 ， 日期：" + date);


        List<String> fileList = new ArrayList<>();

        String tempLine;
        try {
            // 拼接文件路径
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"));
            // 读取文件
            while( (tempLine = br.readLine()) != null) {
                fileList.add(tempLine);
            }
            br.close();
        } catch (IOException e) {
            log.info("读取文件失败 , " + e.getMessage());
            log.error(e);
            System.exit(1);
            return ;
        }

        log.info("========进入文件导入======");
        StringBuffer sbs = new StringBuffer();
        for (String item : fileList) {
            sbs.append(item + "\n");
        }
        String cof = sbs.toString();

        InputStream inputStream;

        try {
            inputStream = new ByteArrayInputStream(cof.getBytes("GBK"));
            String str = MrAzuSplitUtil.readFileByChars(inputStream, "GBK");


            String[] dataBlock = str.split("==============================");

            log.info("dataBlock.length" + dataBlock.length);

            List<Object[]> list = new ArrayList<>(1000);

            for (int j = 0; j < dataBlock.length; j++) {
                if (dataBlock[j] == null || dataBlock[j].length() == 0) {
                    continue;
                }
                String[] gn = dataBlock[j].split("\n");
                //商户编号
                StringBuffer sbTraderCode = new StringBuffer();
                ///商户名称
                StringBuffer sbTraderName = new StringBuffer();
                //清算日期
                StringBuffer sbliqiDate = new StringBuffer();
                //生成日期
                StringBuffer sbcreaDate = new StringBuffer();
                //当前年
                String thisYear = new SimpleDateFormat("yyyy").format(new Date());
                log.info("第" + j + "个数据块的的条数" + gn.length);


                for (int k = 2; k < gn.length; ) {
                    int flag = 0;
                    switch (k) {
                        case 3:
                            //获取无空格条目
                            String strOne = MrAzuSplitUtil.splitForString(gn[k], " ");
                            String[] strTwo = strOne.split("商户名称：");
                            sbTraderCode.append(strTwo[0].replace("商户编号：", ""));

                            sbTraderName.append(strTwo[1].split("收单机构：")[0]);

                            String sql = "select * from TBL_PAY_MERCHANTCHANNEL where channelCode='UNIONPAYQRCODE' and superMerchantNo=?";
                            List<Map<String, Object>> mList = queryForList(sql, new Object[]{sbTraderCode.toString().trim()});

                            String merchantChannelConfigSql = "SELECT * from TBL_PAY_MERCHANTCHANNELCONFIG where superMerchantNo=?";
                            Map<String, Object> mcc = queryForMap(merchantChannelConfigSql, new Object[]{sbTraderCode.toString().trim()});


                            if (mList.size() == 0 && mcc == null) {
                                flag = 1;
                                log.info("商户号：" + sbTraderCode.toString() + "非银联二维码上游商户号，跳过");
                            }
                            k++;
                            break;
                        case 5:
                            //获取无空格条目
                            String strThree = MrAzuSplitUtil.splitForString(gn[k], " ");
                            String[] strFour = strThree.split("生成日期：");
                            sbliqiDate.append(strFour[0].replace("清算日期：", ""));
                            sbcreaDate.append(strFour[1]);
                            k++;
                            continue;
                        case 12:
                            do {
                                if ("".equals(gn[k].trim())) {
                                    break;
                                }
                                String strSix = MrAzuSplitUtil.splitForStringByCondition(gn[k], " ", "@@@", 1);
                                String[] strFive = strSix.split("@@@");

                                if ("消费撤消".equals(strFive[11]) || strFive[11].startsWith("差错")) {
                                    k++;
                                    continue;
                                }


                                if("0.00".equals(strFive[5])){
                                    k++;
                                    continue;
                                }


                                String transDate = strFive[1];
                                String before = thisYear + "-" + transDate.substring(0, 4);
                                String time = transDate.substring(4, 10);
                                StringBuilder sb = new StringBuilder(time);
                                StringBuilder sbDate = new StringBuilder(before);

                                String datetime = sbDate.insert(7, "-").toString() + " " + sb.insert(2, ":").insert(5, ":").toString();

                                String cardNo = strFive[2];
                                String cardType = strFive[3];
                                String bankName = strFive[4];

                                String trackingNo = strFive[9];


                                Object[] obj = new Object[]{strFive[8], new BigDecimal(strFive[5]).setScale(2, BigDecimal.ROUND_HALF_UP),
                                        datetime, new BigDecimal(strFive[6].substring(1)).setScale(2, BigDecimal.ROUND_HALF_UP), CHANNEL_ID,
                                        date,cardNo , cardType, bankName , trackingNo};
                                list.add(obj);

                                if (list.size() >= 1000) {
                                    String saveReconciliationSql = "insert into tbl_online_reconciliation "
                                            + " ( optimistic ,bankorderid , trxAmount , trxDate , trxFee , channelName , dateFlag , cardno, cardtype , bankName , unno) "
                                            + " values (0 , ? , ? , ? , ? , ? , ?  , ? , ? , ? , ?) ";

                                    executeBatchSql(saveReconciliationSql, list);
                                    list.clear();
                                }
                                k++;
                                continue;
                            } while (k < gn.length);
                            break;
                        default:
                            k++;
                            continue;
                    }
                    if (flag == 1) {
                        break;
                    }
                }
            }

            if (list.size() > 0) {
                String saveReconciliationSql = "insert into tbl_online_reconciliation "
                        + " ( optimistic ,bankorderid , trxAmount , trxDate , trxFee , channelName , dateFlag , cardno, cardtype , bankName , unno) "
                        + " values (0 , ? , ? , ? , ? , ? , ?  , ? , ? , ? , ?) ";

                executeBatchSql(saveReconciliationSql, list);
                list.clear();
            }

            String sql = "select max(id)+1 as maxID from tbl_online_reconciliation" ;
            String maxID = queryForMap(sql, null).get("maxID").toString();

            log.info("开始更新序列, 序列值为：" +  maxID);

            sql = "update sequence_online_reconciliation set next_val=? ";
            executeSql(sql, new Object[]{maxID});

        } catch (Exception e) {
            log.info(e);
            System.exit(1);
            return ;

        }
    }


    public void copyOrder(){

        Long start = System.currentTimeMillis();

        log.info("开始拷贝无卡快捷数据 , 开始时间:" +  start);

        String insertCopyOrder = " insert into tbl_online_copyorder   (optimistic,b2bOrB2c,bankId,debitOrCredit,channelCompleteDate,kuaiType,"
                + "merchantNo,orderAmount,receiverFee,orderIp,orderNumber,createDate,completeDate,bankOrderId,orderStatus,"
                + "payType,payerFee,trxType , withdrawStatus,cost,mode,t0PayResult,merchantId,withdrawAmount,withdrawFee,superMerchantNo,"
                + "channelId,withdrawNo,withdrawMsg,checkScanCodeStatus,checkWithdrawStatus , withdrawTime) "
                + "select optimistic,b2bOrB2c,bankId,debitOrCredit,channelCompleteDate,kuaiType,"
                + "merchantNo,orderAmount,receiverFee,orderIp,orderNumber,createDate,completeDate,bankOrderId,orderStatus,"
                + "payType,payerFee, trxType , withdrawStatus,cost,mode,t0PayResult,merchantId,withdrawAmount,withdrawFee,superMerchantNo,"
                + "channelId,withdrawNo,withdrawMsg , 'INIT' , 'INIT' , '"+date+"' from tbl_online_order where ((orderStatus='SUCCESS' and channelId='rytPayKuai')  or  channelId='UNIONPAYQRCODE') 	"
                + " and  createDate >= '"+date+" 23:00:00'-INTERVAL 23 HOUR  and   createDate < '"+date+" 23:00:00'";

        log.info("执行的sql：" + insertCopyOrder);

        int x = executeSql(insertCopyOrder, null);


        log.info("转移完成，受影响行数：" +  x );


        String sql = "select max(id)+1 as maxid from tbl_online_copyorder" ;
        String maxid = queryForMap(sql, null).get("maxid").toString();

        log.info("开始更新序列, 序列值为：" +  maxid);

        sql = "update sequence_online_copyorder set next_val=? ";
        executeSql(sql, new Object[]{maxid});

        Long end = System.currentTimeMillis();
        Long time = (end - start)/1000;

        log.info("  转移无卡快捷任务结束========================  共使用时间:" + time + "秒 ");
    }


    public void checkOrder(){

        String success = "update TBL_ONLINE_COPYORDER  as a ,  tbl_online_reconciliation  as b  " +
                " set a.orderstatus='SUCCESS' , a.serialNumber = substr(a.bankOrderId , 23 , 12) , a.checkAmount = b.trxAmount ," +
                " a.checkFee = b.trxFee , a.checkScanCodeStatus= 'SUCCESS' ,  a.channelId = b.channelName , a.cardNo=b.cardno ," +
                " a.toibkn=b.cardtype , a.payerName=b.bankName  , a.desciption=b.unno " +
                " where SUBSTR(a.bankOrderId ,23,12)=b.bankOrderId and withdrawTime='"+date+"' and a.channelId='UNIONPAYQRCODE' and  date(b.dateFlag)=date('"+date+"') " +
                " and a.orderStatus='SUCCESS' ";

        log.info("成功sql ：" +  success);
        int ret = executeSql(success , null);
        log.info("对账成功受影响行数：" + ret);


        if(ret < 0){

            log.info("处理成功数据异常  ，  程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" , new String[]{"zhoufangyu@ronghuijinfubj.com" ,  "zhangzhiguo@ronghuijinfubj.com"} , null , null );

            System.exit(1);
        }

        String dropOrder = "update TBL_ONLINE_COPYORDER  as a ,  tbl_online_reconciliation  as b " +
                " set a.orderstatus='SUCCESS' ,  a.bankName='falg_fail', a.serialNumber = substr(a.bankOrderId , 23 , 12) , " +
                " a.checkAmount = b.trxAmount , a.checkFee = b.trxFee , a.checkScanCodeStatus= 'SUCCESS' ,  a.channelId = b.channelName ," +
                " a.cardNo=b.cardno , a.toibkn=b.cardtype , a.payerName=b.bankName , a.desciption=b.unno" +
                " where SUBSTR(a.bankOrderId ,23,12)=b.bankOrderId and withdrawTime='"+date+"' and a.channelId='UNIONPAYQRCODE' and " +
                " date(b.dateFlag)=date('"+date+"') and a.orderStatus='INIT' ";

        log.info("调单sql：" + dropOrder);
        ret = executeSql(dropOrder , null);

        log.info("调单受影响行数:" + ret);

        if(ret < 0){
            log.info("处理调单数据异常 ， 程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" , new String[]{"zhoufangyu@ronghuijinfubj.com" ,  "zhangzhiguo@ronghuijinfubj.com"} , null , null );
            System.exit(1);
        }

        String longData = "insert into tbl_online_copyorder  " +
                " (bankOrderId , checkAmount , checkFee , checkScanCodeStatus , checkWithdrawStatus , cost , createDate , orderStatus , channelId ,payerFee ," +
                " receiverFee , orderWithdrawAmount , orderWithdrawFee , t0PayResult, orderAmount , withdrawTime , mode , cardNo  , toibkn  , payerName , desciption )" +
                " select bankOrderId , trxAmount , trxFee , 'LONG' , 'INIT' , 0 , trxDate , 'SUCCESS' ,  'UNIONPAYQRCODE' , 0 , 0 , 0 , 0 , 0  , 0 , '"+date+"' , 0  , cardno ," +
                " cardtype , bankName , unno"  +
                " from tbl_online_reconciliation  where bankOrderId not in " +
                " ( " +
                " select SUBSTR(bankOrderId , 23,12) from  TBL_ONLINE_COPYORDER  where withdrawTime='"+date+"' and channelid='UNIONPAYQRCODE' " +
                " ) " +
                " and date(dateFlag)='"+date+"'  and channelName='UNIONPAYQRCODE' ;";

        log.info("长款sql：" + longData);
        ret = executeSql(longData , null);
        log.info("长款受影响行数：" + ret);

        if(ret < 0){
            log.info("处理长款数据异常 ， 程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" , new String[]{"zhoufangyu@ronghuijinfubj.com" , "zhangzhiguo@ronghuijinfubj.com"} , null , null );
            System.exit(1);
        }

        String shortData = "update tbl_online_copyorder  set checkScanCodeStatus='SHORT'" +
                "  where withdrawTime='"+date+"' and orderStatus='SUCCESS' and checkScanCodeStatus='INIT' and SUBSTR(bankOrderId , 23,12) not in " +
                " (select bankOrderId from tbl_online_reconciliation  where DATE(dateFlag)='"+date+"' )  and channelId='UNIONPAYQRCODE' ";

        log.info("短款sql： " + shortData);
        ret = executeSql(shortData , null);

        log.info("短款受影响行数：" + ret);


        if(ret < 0){

            log.info("处理长款数据异常  程序停止");

            SendMail.sendMail("sql执行异常" , "sql执行异常" , new String[]{"zhoufangyu@ronghuijinfubj.com" ,  "zhangzhiguo@ronghuijinfubj.com"} , null , null );
            System.exit(1);
        }

        log.info("更新copy订单表序列");
        String sql = "select max(id)+1 as maxid from tbl_online_copyorder" ;
        String maxid = queryForMap(sql, null).get("maxid").toString();

        log.info("开始更新序列, 序列值为：" +  maxid);

        sql = "update sequence_online_copyorder set next_val=? ";
        executeSql(sql, new Object[]{maxid});

        sql = "delete from tbl_online_copyorder where bankName is null and orderStatus='INIT' and withdrawTime='"+date+"' " ;
        ret = executeSql(sql , null);

        log.info("删除日期：" + date + " 冗余数据：   " + ret + "    条数");

    }


    public static void main(String[] args) {

        Long startTime = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE , -1);
        String date = sdf.format(c.getTime());

        if(args.length > 0 ){
            date = args[0];
        }

        CheckBills checkBills = new CheckBills(date);
        // 读取unionpay 对账文件
        checkBills.init();
        // 执行对账操作
        checkBills.checkOrder();

        new CheckBillsCommaSeparatedValues(date);

        Long endTime = System.currentTimeMillis();

        System.out.println("本次对账使用时间：" + (endTime - startTime) / 1000.0 + " 秒");

    }
}
