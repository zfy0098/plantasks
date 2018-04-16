package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by hadoop on 2018/3/23.
 *
 * @author hadoop
 */
public class CalculateTradeAmount extends OnlineBaseDao {


    public CalculateTradeAmount(String date) {
        String filePath = "/merchant.txt";
        String temp;
        StringBuffer sbf = new StringBuffer("(");

        try {
            File file = new File(filePath);

            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((temp = reader.readLine()) != null) {
                log.info(" temp :  " + temp);
                sbf.append("'");
                sbf.append(temp);
                sbf.append("',");
            }

            reader.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String merchantList = sbf.toString().substring(0 , sbf.toString().length()-1) + ")";

        StringBuffer sql = new StringBuffer("select sum(orderAmount)  as orderAmount , superMerchantNo " +
                "from tbl_online_copyorder " +
                " where date(withdrawTime)='"+date+"' and channelId='UNIONPAYQRCODE' and trxType='UNIONPAY_QRCODE_LARGE' and toibkn!='借记账户' and toibkn is not null ");
        sql.append(" and superMerchantNo in ");
        sql.append(merchantList);
        sql.append(" group by superMerchantNo");

        log.info(sql.toString());



//        List<Map<String,String>> list = queryForList(sql.toString());
//
//        for (int i = 0; i < list.size() ; i ++ ){
//            Map<String,String> map = list.get(i);
//            log.info(" superMerchantNo :" + map.get("superMerchantNo") + " , ======= orderAmount :" + map.get("orderAmount"));
//
//        }
    }


    public static void main(String[] args) {
        String date = "2018-03-14";
        new CalculateTradeAmount(date);
    }


}
