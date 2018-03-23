package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.DateUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by hadoop on 2018/3/22.
 *
 * @author hadoop
 */
public class JointCheckBills extends OnlineBaseDao{


    private static final String CHANNEL_ID = "UNIONPAYQRCODE";

    private String date;

    public JointCheckBills(String date){
        this.date = date;
    }

    public void init(){


        String fileDate = date.replace("-" , "").substring(2);

        String filePath =  "/opt/sh/unionpay/duizhang_file/IND" +fileDate + "01ACOM";


        log.info("获取 yyMMdd 格式的时间:" + fileDate);
        log.info("获取文件路径：" + filePath);
        log.info("开始执行银联二维码对账 ， 日期：" + date);


        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        SimpleDateFormat sdfs = new SimpleDateFormat("MM-dd HH:mm:ss");

        List<Object[]> list = new ArrayList<>(1000);

        String tempLine;
        try {
            // 拼接文件路径
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"));
            // 读取文件
            for (int i = 0; (tempLine = br.readLine()) != null; i++) {
                tempLine = tempLine.replace(" " , "@");
                String[] data = tempLine.split("@");

                StringBuffer sbf = new StringBuffer();
                for(int j = 0 ; j < data.length ; j++ ){
                    if("".equals(data[j])){
                        continue;
                    }else{
                        sbf.append(data[j]);
                        sbf.append("@");
                    }
                }

                log.info("第：" + i + " 行数据 ： " + sbf.toString());

                data = sbf.toString().split("@");
                String bankOrderid = data[13];

                double trxAmount = AmountUtil.div(data[5] , "100" , 2);
                Date riqi = null;
                try {
                    riqi = sdf.parse(data[3]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String trxDate = DateUtil.getNowTime("yyyy") + "-" +sdfs.format(riqi);

                double trxFee =  AmountUtil.div(data[7] , "100" , 2);
                String unno = data[2];
                String cardNo = data[4];



                Object[] obj = new Object[]{bankOrderid, trxAmount,
                        trxDate, trxFee, CHANNEL_ID,
                        date,cardNo  , unno};
                list.add(obj);


                if (list.size() >= 1000) {
                    String saveReconciliationSql = "insert into tbl_online_reconciliation_test  "
                            + " ( optimistic ,bankorderid , trxAmount , trxDate , trxFee , channelName , dateFlag , cardno  , unno) "
                            + " values (0 , ? , ? , ? , ? , ? , ?  , ?  , ?) ";

                    executeBatchSql(saveReconciliationSql, list);
                    list.clear();
                }
            }
            br.close();
        } catch (IOException e) {
            log.info("读取文件失败 , " + e.getMessage());
            log.error(e);
            System.exit(1);
            return ;
        }

        if (list.size() > 0) {
            String saveReconciliationSql = "insert into tbl_online_reconciliation_test "
                    + " ( optimistic ,bankorderid , trxAmount , trxDate , trxFee , channelName , dateFlag , cardno  , unno) "
                    + " values (0 , ? , ? , ? , ? , ? , ?  , ?  , ?) ";

            executeBatchSql(saveReconciliationSql, list);
        }

    }


    public static void main(String[] args) throws  Exception{

        Long startTime = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE , -1);
        String date = sdf.format(c.getTime());

        if(args.length > 0 ){
            date = args[0];
        }


        JointCheckBills jointCheckBills = new JointCheckBills(date);
        // 读取unionpay 对账文件
        jointCheckBills.init();


        // 执行对账操作
//        checkBills.checkOrder();


        Long endTime = System.currentTimeMillis();

        System.out.println("本次对账使用时间：" + (endTime - startTime) / 1000.0 + " 秒");


}

}
