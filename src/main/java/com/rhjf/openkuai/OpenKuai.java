package com.rhjf.openkuai;

import com.rabbitmq.client.AMQP;
import com.rhjf.base.BaseDao;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/3/22.
 *
 * @author hadoop
 */
public class OpenKuai extends BaseDao {

    public void init() {
        OnlineOpenKuai onlineOpenKuai = new OnlineOpenKuai();

        String sql = "select * from tab_openkuai where cvn2=''";
        List<Map<String, Object>> list = queryForList(sql, null);
        for (int i = 0; i < list.size(); i++) {
            String bankCardNo = list.get(i).get("bankCardNo").toString();

            log.info("卡号：" + bankCardNo);

            Map<String, Object> map = onlineOpenKuai.getBankCardNoCvn2(bankCardNo);

            if (map != null && !map.isEmpty()) {
                String cvn2 = map.get("cvn2").toString();
                String expired = map.get("expired").toString();

                String newExpired = expired.substring(2) + expired.substring(0,2);

                log.info("卡号：" + bankCardNo + " ==== cvn2 ：" + cvn2 + "==== 有效期:" + expired + "==newExpired:" + newExpired);


                String update = "update tab_openkuai set cvn2=? , expired=? where bankCardNo=?";
                executeSql(update, new Object[]{cvn2, newExpired, bankCardNo});

            }

        }
    }


    public static void main(String[] args){
        OpenKuai openKuai = new OpenKuai();
        openKuai.init();

    }

}
