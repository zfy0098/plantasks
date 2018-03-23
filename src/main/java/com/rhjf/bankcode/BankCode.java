package com.rhjf.bankcode;

import com.rhjf.base.BaseDao;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/3/22.
 *
 * @author hadoop
 */
public class BankCode extends BaseDao {

    public void init() {

        String sql = "select a.bankName as aname , a.bankCode as acode , b.BankName , b.BankSymbol as banksymbol from (select bankName , bankCode from tab_pay_binverify GROUP BY bankName) as a , " +
                "(select BankName , BankSymbol from tab_pay_bankcode GROUP BY BankName) as b where b.BankName like CONCAT('%' , a.bankName , '%')  and a.bankCode!=b.BankSymbol ";

        List<Map<String, Object>> list = queryForList(sql, null);

        for (int i = 0; i < list.size(); i++) {

            Map<String, Object> map = list.get(i);

            log.info("aname :" + map.get("aname") + "============= acode :" + map.get("acode") + " ================ b.bankname :" + map.get("BankName") + " , =============" + map.get("banksymbol"));

            String update = "update tab_pay_binverify set bankCode=? where bankCode=? and bankName=? ";
            executeSql(update, new Object[]{map.get("banksymbol"), map.get("acode"), map.get("aname")});
        }
    }


    public static void main(String[] args) {
        BankCode bankCode = new BankCode();
        bankCode.init();
    }
}
