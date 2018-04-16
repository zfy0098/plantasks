package com.rhjf.app;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.DateUtil;
import com.rhjf.utils.UtilsConstant;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hadoop on 2018/3/26.
 *
 * @author hadoop
 */
public class AppJIFUCheck extends BaseDao {

    public void init() {

        String filePath = "/opt/sh/1.csv";
        String yesterday;

        try {
            yesterday = DateUtil.getDateAgo(DateUtil.getNowTime(DateUtil.yyyy_MM_dd), 1, DateUtil.yyyy_MM_dd);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        List<Object[]> list = new ArrayList<>(30);

        try {
            FileInputStream fs = new FileInputStream(filePath);
            InputStreamReader is = new InputStreamReader(fs, "GBK");
            BufferedReader bf = new BufferedReader(is);
            String temp;
            bf.readLine();
            while ((temp = bf.readLine()) != null) {

                String[] data = temp.split(",");

                String id = UtilsConstant.getUUID();
                String orderNumber = data[3].substring(1);

                String amount = data[4].contains("\"") ? (data[4] + data[5]).replace(",", "").replace("\"", "") : data[4];
                int checkAmount = (int) AmountUtil.mul(amount, "100");

                String fee = data[7];

                if (data.length == 13) {
                    fee = data[8];
                }
                int checkFee = (int) AmountUtil.mul(fee, "100");

                StringBuffer date = new StringBuffer(data[0]);
                date.insert(4, "-");
                date.insert(7, "-");

                yesterday = date.toString();

                Object[] obj = new Object[]{id, "HTu3kLl0", orderNumber, checkAmount, checkFee, "0000", "", date.toString(), "JIFU"};
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        String saveReconciliation = "insert into tab_order_reconciliation (ID,MerchantID,OrderNumber,CheckAmount ,CheckFee,CheckOrderStatus,CheckWithdrawStatus,TradeDate , ChannelID) "
                + " values (?,?,?,?,?,?,?,?,?)";

        executeBatchSql(saveReconciliation, list);


        String copySql = "insert into tab_pay_copyorder (id , amount , TradeDate , TradeTime , TermSerno , TradeType , TradeCode , UserID , ChannelID , PayChannel , FeeRate , MerchantID , Fee , OrderNumber ) " +
                "select id , amount , TradeDate , TradeTime , TermSerno , TradeType , TradeCode , UserID , ChannelID , PayChannel , FeeRate , MerchantID , Fee , OrderNumber   from tab_pay_order where TradeDate=? and channelID='JIFU' and PayRetCode='0000' ";
        executeSql(copySql, new Object[]{yesterday.replace("-", "")});



        /* 执行对账操作 */
        String checkSql = "update tab_pay_copyorder as tpc , tab_order_reconciliation as tor "
                + " set tpc.CheckAmount=tor.CheckAmount , tpc.CheckFee=tor.CheckFee , tpc.CheckStatus=tor.CheckOrderStatus , tpc.T0CheckStatus=tor.CheckWithdrawStatus"
                + " where tpc.OrderNumber=tor.OrderNumber and tor.TradeDate=?  and tpc.ChannelID='JIFU'";
        executeSql(checkSql, new Object[]{yesterday});


		/*处理长款数据  */
        String longTradeSql = "insert into tab_pay_copyorder "
                + " (ID , amount , tradedate,tradetime , termserno , tradetype,tradecode   ,merchantid , fee,ordernumber , CheckAmount , CheckFee,CheckStatus,T0CheckStatus , ChannelID) "
                + " select   UPPER(UUID()) , 0 , ? , '' , '' , '' , '' , MerchantID ,0 , OrderNumber , CheckAmount , CheckFee , '1000' , '' , 'JIFU' "
                + " from tab_order_reconciliation where OrderNumber not in (select OrderNumber from tab_pay_copyorder where TradeDate=?) and TradeDate=?";
        executeSql(longTradeSql, new Object[]{yesterday.replace("-", ""), yesterday.replace("-", ""), yesterday});


        String updateUserID = "update tab_pay_copyorder as tpco, tab_pay_order as tpo set tpco.userID = tpo.userID , tpco.PayChannel=tpo.payChannel "
                + "where  tpco.UserID is null and tpco.ChannelID='JIFU'";

        executeSql(updateUserID, null);

    }

    public static void main(String[] args) {
        AppJIFUCheck jifuCheck = new AppJIFUCheck();
        jifuCheck.init();
    }
}