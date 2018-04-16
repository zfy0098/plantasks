package com.rhjf.pos;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.DateUtil;
import com.rhjf.utils.ExcelReader;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.MD5;
import com.rhjf.utils.RandomUtils;
import net.sf.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hadoop on 2018/4/8.
 *
 * @author hadoop
 */
public class PosT1Withdraw extends OnlineBaseDao {


    /**
     * 代付应用名称
     */
    public static final String WITH_DRAW_APPLICATION = "Withdrawals.Req";
    /**
     * 代付产品类型
     */
    public static final String WITH_DRAW_BIZ_TYPE = "030000";
    /**
     * 代付交易类型
     */
    public static final String WITH_DRAW_TRANS_TYPE = "030100";
    /**
     * 代付子交易类型
     */
    public static final String WITH_DRAW_SUB_TRANS_TYPE = "030101";
    /**
     * 代付请求地址
     */
    public static final String WITH_DRAW_REQ_URL = "https://front.chinagpay.com/org.ac";

    /**
     * 渠道编号
     */
    public static final String CHANNEL_ID = "600000000318";

    /**
     * 商户编号
     */
    public static final String MERCHANT_ID = "929060095081597";

    /**
     * /终端类型
     */
    public static final String TERMINAL_OS = "03";

    /**
     * 银行卡标志
     */
    public static final String PP_FLAG = "01";

    /**
     * 交易币种
     */
    public static final String CURRENCY = "156";


    /**
     * 加密密钥
     */
    public static final String KEY = "9F7868C66CF2D0C8EDAFEE27FE2BBCB5";


    /**
     * 线上平台代理商户id
     */
    public static final String ONLINE_AGENT_ID = "253";

    private String path;

    public PosT1Withdraw(String path) {
        this.path = path;
    }


    public void init() throws Exception {

        ExcelReader excelReader = new ExcelReader(path);
        List<String[]> list = excelReader.getAllData(0);

        int listSize = 8;
        if (list != null && list.size() >= listSize) {
            for (int i = 7; i < list.size(); i++) {
                String[] data = list.get(i);

                log.info(" 需要代付的信息 :" + Arrays.toString(data));

                String bankName = data[2];
                String accountNo = data[3];
                String accountName = data[4];
                String amount = data[5];

                String bankOrderId = "WyPay" + System.currentTimeMillis() + RandomUtils.getRandomString(7);

                String time = DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss);

                Map<String, Object> head = new HashMap<>(16);
                head.put("accept", "application/json");
                head.put("Proxy-Connection", "Keep-Alive");
                head.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");


                Map<String, Object> map = new TreeMap<>();
                map.put("application", WITH_DRAW_APPLICATION);
                map.put("sendTime", time);
                map.put("terminalOs", TERMINAL_OS);
                map.put("channelId", CHANNEL_ID);
                map.put("merchantId", MERCHANT_ID);
                map.put("orderId", bankOrderId);
                map.put("bizType", WITH_DRAW_BIZ_TYPE);
                map.put("transType", WITH_DRAW_TRANS_TYPE);
                map.put("subTransType", WITH_DRAW_SUB_TRANS_TYPE);
                map.put("accNo", accountNo);
                map.put("customerNm", accountName);
                map.put("ppFlag", PP_FLAG);
                int transAmt = (int) AmountUtil.mul(String.valueOf(AmountUtil.add(amount, "0.5")), "100");
                map.put("transAmt", transAmt);
                map.put("currency", CURRENCY);
                StringBuilder signString = new StringBuilder();


                for (Map.Entry entry : map.entrySet()) {
                    signString.append(entry.getKey());
                    signString.append("=");
                    signString.append(map.get(entry.getKey().toString()));
                    signString.append("&");
                }

                log.info("爱农代付待加密字符串---------" + signString.toString());
                String sign = null;
                try {
                    sign = MD5.sign(signString.append("key=" + KEY).toString(), "utf-8").toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("爱农代付--------请求签名为：" + sign);
                map.put("sign", sign);

                log.info("请求报文：" + JSONObject.fromObject(map));
                String content = HttpClient.post(WITH_DRAW_REQ_URL, head, map, null);

                log.info("响应报文：=====   " + content);
                JSONObject json = JSONObject.fromObject(content);
                String key = "respCode";
                String respCode = "0000";
                String withdrawStatus = "fail";
                String message;
                if (respCode.equals(json.getString(key))) {
                    withdrawStatus = "success";
                    message = "成功";
                } else {
                    message = json.getString("respDesc");
                }

                String sql = "insert into tbl_online_withdraw_order (optimistic , agentNo , bankName , bankOrderId , cardNo , createDate , payerName , receiverFee , " +
                        " trxType , withdrawAmount , withdrawMsg , withdrawStatus , channel ) values" +
                        " (?,?,?,?,?,now(),?,?,?,?,?,?,?)";
                executeSql(sql, new Object[]{1, ONLINE_AGENT_ID, bankName, bankOrderId, accountNo, accountName, "0.5", "UnionPay", amount, message, withdrawStatus, 2});

                log.info("开始更新序列, 序列值为：");

                sql = "update sequence_online_withdraw_order set next_val=next_val+1 ";
                executeSql(sql, null);
            }
        } else {
            log.info("数据格式错误，行数小于 8 行");
        }
    }


    public static void main(String[] args) throws Exception {
        PosT1Withdraw posT1Withdraw = new PosT1Withdraw("/1.xls");
        posT1Withdraw.init();
    }
}
