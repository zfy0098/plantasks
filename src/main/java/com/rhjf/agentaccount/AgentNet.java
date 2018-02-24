package com.rhjf.agentaccount;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.CreateExcle;
import com.rhjf.utils.DateUtil;
import com.rhjf.utils.UtilsConstant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/12/18.
 *
 * @author hadoop
 */
public class AgentNet extends OnlineBaseDao{

    public void init() throws  Exception{
        log.info("程序开始执行");

        String staDate = "2017-10-01";
        String endDate = DateUtil.getNowTime(DateUtil.yyyy_MM_dd);

        List<String> dateList = DateUtil.getBetweenDates(staDate , endDate , DateUtil.yyyy_MM_dd);

        /**
         * 284 311 299 297
         */
        String agentListSQL = "SELECT * from tbl_pay_agent where isSupportWithdraw=1 ";

        List<Map<String,Object>> agentList = queryForList(agentListSQL , null);

        List<Object[]> list = new ArrayList<>();


        List<Object[]> params = new ArrayList<>();
        for (int j = 0; j < agentList.size(); j++) {

            String agentID = UtilsConstant.ObjToStr(agentList.get(j).get("id"));

            String tradeAmontSql =  "select sum(orderAmount-receiverFee) as amount from " +
                    " (select * from tbl_online_order where orderStatus='SUCCESS' and date(createDate) BETWEEN '2017-10-01' and date(now()) and  b2bOrB2c='NET' and t0PayResult=1) as  too " +
                    " INNER JOIN (select * from tbl_pay_merchant where agent_id=? ) as tpm on too.merchantNo=tpm.merchantNo ";

            String tradeAmont = UtilsConstant.strIsEmpty(UtilsConstant.ObjToStr(queryForMap(tradeAmontSql , new Object[]{ agentID}).get("amount")))?"0":
                    queryForMap(tradeAmontSql , new Object[]{agentID}).get("amount").toString();


            if("0".equals(tradeAmont)){
                continue;
            }

            String withdrawAmountSQL = "select sum(withdrawAmount+receiverFee) as withdrawAmount from tbl_online_withdraw_order" +
                    " where agentNo=? and withdrawStatus='success' and trxType='NET' and date(createDate) BETWEEN '2017-10-01' and date(now())";

            String withdrawAmount = UtilsConstant.strIsEmpty(UtilsConstant.ObjToStr(queryForMap(withdrawAmountSQL , new Object[]{ agentID}).get("withdrawAmount")))?"0":
                    queryForMap(withdrawAmountSQL , new Object[]{ agentID}).get("withdrawAmount").toString();

            String accountAmountSql = "select * from tbl_pay_account where ownertype='AGENT' and accountTrxtype='NET' and ownerId=? ";


            Map<String,Object> m = queryForMap(accountAmountSql , new Object[]{ agentID});
            if(m == null){
                continue;
            }

            String accountAmount =  UtilsConstant.strIsEmpty(UtilsConstant.ObjToStr(m.get("balance")))?"0":
                    m.get("balance").toString();



            double blance = AmountUtil.sub(tradeAmont , withdrawAmount);


            double canBalance = blance;

            String yesterday = "select sum(orderAmount-receiverFee) as amount from " +
                    " (select * from tbl_online_order where orderStatus='SUCCESS' and date(createDate)=date('2017-12-27') and  b2bOrB2c='NET' and t0PayResult=1) as  too " +
                    " INNER JOIN (select * from tbl_pay_merchant where agent_id=? ) as tpm on too.merchantNo=tpm.merchantNo ";



            String yesterdayAmount = UtilsConstant.strIsEmpty(UtilsConstant.ObjToStr(queryForMap(yesterday , new Object[]{ agentID}).get("amount")))?"0":
                    queryForMap(yesterday , new Object[]{ agentID}).get("amount").toString();


            canBalance = AmountUtil.sub(String.valueOf(blance) , String.valueOf(AmountUtil.mul(yesterdayAmount , "0.2")));

//            if(AmountUtil.mul(String.valueOf(blance) , "0.2") >= 200000 ){
//                canBalance = blance - 200000;
//            }else{
//                canBalance = (new BigDecimal(String.valueOf(blance)).multiply(new BigDecimal("0.8"))).setScale(2 ,BigDecimal.ROUND_DOWN).doubleValue();
//            }

            params.add(new Object[]{blance , canBalance , agentID});

            log.info("代理商长度：" + agentList.size() + " , 执行坐标:" + j + " ， 代理商名称：" + agentList.get(j).get("signName") + "," +
                    "交易金额：" + tradeAmont  + " , 提现手续费加金额： "   + withdrawAmount + " , 账户余额：" + accountAmount + " ， 相差金额：" + blance + ", canBalance：" + canBalance );

//            Object[] obj = new Object[]{agentList.get(j).get("signName") ,tradeAmont  , withdrawAmount , accountAmount , blance};
//            list.add(obj);

            String sql = "update tbl_pay_account set balance=? , canbalance=? where ownerId=? and ownerType='AGENT' and accountTrxType='NET' ";
            executeSql(sql , new Object[]{blance , canBalance , agentID});
        }

//        String sql = "update tbl_pay_account set balance=? , canbalance=? where ownerId=? and ownerType='AGENT' and accountTrxType='NET' ";
//        executeBatchSql(sql , params);


        String[] title = {"代理商名称" , "交易金额(不包含手续费)" , "提现金额(到账金额)" , "账户余额" ,  "相差金额"};
        CreateExcle.createExcel2(title , list , "/" , DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss) + "NET");
    }


    public static void main(String[] args) throws Exception{
        AgentNet agentUnionPay = new AgentNet();
        agentUnionPay.init();
    }



}
