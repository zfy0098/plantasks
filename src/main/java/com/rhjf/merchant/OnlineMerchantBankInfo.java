package com.rhjf.merchant;

import com.rhjf.base.OnlineBaseDao;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/12/12.
 *
 * @author hadoop
 */
public class OnlineMerchantBankInfo extends OnlineBaseDao {



    public List<Map<String,Object>> init(){
        String sql = "select a.merchantno , b.* from tbl_pay_merchant as a INNER JOIN tbl_pay_merchantbankcard as b " +
                " on a.id=b.ownerId where agent_id='178' and a.merchantStatus='AVAILABLE'";
        List<Map<String,Object>> list = queryForList(sql , null);
        return  list;
    }

}
