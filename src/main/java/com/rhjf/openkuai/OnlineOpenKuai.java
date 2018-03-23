package com.rhjf.openkuai;

import com.rhjf.base.BaseDao;
import com.rhjf.base.OnlineBaseDao;

import java.util.Map;

/**
 * Created by hadoop on 2018/3/22.
 *
 * @author hadoop
 */
public class OnlineOpenKuai extends OnlineBaseDao{


    public Map<String,Object> getBankCardNoCvn2(String bankCardNo){
        String sql = "select * from tbl_online_open_kuai where channelId='CJ' and accountNo=? and cvn2 is not null";
        return queryForMap(sql , new Object[]{bankCardNo});
    }
}
