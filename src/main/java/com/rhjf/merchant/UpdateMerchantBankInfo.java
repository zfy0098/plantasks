package com.rhjf.merchant;

import com.rhjf.base.BaseDao;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/12/12.
 *
 * @author hadoop
 */
public class UpdateMerchantBankInfo extends BaseDao{

    private Logger log = Logger.getLogger(this.getClass());

    public void init(){
        OnlineMerchantBankInfo onlineMerchantBankInfo = new OnlineMerchantBankInfo();
        List<Map<String,Object>> onlineMerchantBankInfoList =  onlineMerchantBankInfo.init();

        String sql = "select  b.MerchantID , a.* " +
                " from tab_pay_userbankcard as a INNER JOIN (select UserID , MerchantID from tab_pay_merchant group by userid) as b on a.UserID=b.UserID";
        List<Map<String , Object>> bankInfoList = queryForList(sql , null);


        List<Object[]> list = new ArrayList<>(bankInfoList.size());

        for (int i = 0 ; i < onlineMerchantBankInfoList.size() ; i++){

            Map<String,Object> onlineMap = onlineMerchantBankInfoList.get(i);

            for (int j = 0; j < bankInfoList.size(); j++) {

                Map<String,Object> appBankInfo = bankInfoList.get(j);

                String onlineMerchantNO = onlineMap.get("merchantno").toString();
                String appMerchantNo = appBankInfo.get("MerchantID").toString();

                if(onlineMerchantNO.equals(appMerchantNo)){

                    String accountNo = onlineMap.get("accountNo").toString();
                    String appAccountNo = appBankInfo.get("AccountNo").toString();

                    if(!accountNo.equals(appAccountNo)){
                        log.info("商户：" + appMerchantNo + "结算卡卡号不一致， 平台结算卡号：" + accountNo + " , app平台结算卡号：" + appAccountNo);

                        String bankBranch = onlineMap.get("bankBranch").toString();
                        String bankProv = onlineMap.get("bankProv").toString();
                        String bankCity = onlineMap.get("bankCity").toString();
                        String bankCode = onlineMap.get("bankCode").toString();
                        String bankName = onlineMap.get("bankName").toString();
                        String bankSymbol = onlineMap.get("bankSymbol").toString();

                        String userID = appBankInfo.get("UserID").toString();
                        Object[] obj = new Object[]{accountNo ,bankBranch , bankProv , bankCity , bankCode ,bankName , bankSymbol ,  userID };
                        list.add(obj);
                    }
                    bankInfoList.remove(j);
                    j--;
                }
            }
        }

        if(list.size() > 0){
            String update = "update tab_pay_userbankcard set AccountNo=? , BankBranch = ? , BankProv=? , BankCity=? , BankCode=? , BankName = ? , BankSymbol = ? where UserID=? ";
            executeBatchSql(update , list);
        }


    }


    public static void main(String [] args){
        UpdateMerchantBankInfo merchantBankInfo = new UpdateMerchantBankInfo();
        merchantBankInfo.init();
    }


}
