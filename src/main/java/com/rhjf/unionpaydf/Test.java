package com.rhjf.unionpaydf;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hadoop on 2018/2/2.
 *
 * @author hadoop
 */
public class Test {

    static Logger logger = Logger.getLogger(Test.class);

    //测试环境
//  private static final String url = "211.103.172.38:8830";
    /**
     * 	生产环境
     */
    private static final String URL = "144.112.33.229:8830";

    private static final String password = "1234";

//  private static final String signCert = "/usr/local/test1024.pfx";

    private static final String signCert = "/usr/local/xydf.pfx";

    private static final Integer timeOut = 180;


    public static void main(String[] args){
        HashMap<String, String> queryMap = new HashMap<String, String>();
        Map<String, String> retQueryMap = new HashMap<String, String>();
        queryMap.put("version", "1.0");// 版本号
        queryMap.put("txnType", "00");// 交易类型
        queryMap.put("merId", "309113148163013");// 商户代码
        queryMap.put("signMethod", "01");// 交易子类
        queryMap.put("enterpriseNo", "18701");// 企业编号
        queryMap.put("orderId", "UnionPay1517535562097H4W6Jj0");

        queryMap.put("txnTime", "20180202093922");
        int sllep = 0;
        long l2 = 0;
        long l = System.currentTimeMillis();
        // int timeOut = Integer.parseInt(LoadPro.loadProperties("http",
        // "tjyl_timeOut"));
        while ((l2 - l) < timeOut * 1000) {
            l2 = System.currentTimeMillis();
            retQueryMap = TJYLBase.submitDate(queryMap, URL , signCert, password);
            try {
                if (retQueryMap == null) {
                    // 未返回查询数据时需要重复查询
                    logger.info("代付" + "UnionPay1517535562097H4W6Jj0" + "未收到查询报文,需继续查询交易状态");
                    sllep = sllep + 1;
                    Thread.sleep(sllep * 1000);
                } else if (retQueryMap.get("respCode").equals("00")) {
                    // 查询成功且原交易状态不确认时需要重新发送交易查询
                    if (retQueryMap.get("origRespCode").equals("03")
                            || retQueryMap.get("origRespCode").equals("04")
                            || retQueryMap.get("origRespCode").equals("Z19")) {
                        logger.info("代付" + "UnionPay1517535562097H4W6Jj0"+ "原交易应答码=【"
                                + retQueryMap.get("origRespCode") + "#" + retQueryMap.get("origRespMsg")
                                + "】,需继续查询交易状态");
                        sllep = sllep + 1;
                        Thread.sleep(sllep * 1000);
                    } else {
                        logger.info("代付" + "UnionPay1517535562097H4W6Jj0" + "原交易应答码=【"
                                + retQueryMap.get("origRespCode") + "#" + retQueryMap.get("origRespMsg")
                                + "】，不需要继续查询交易状态");
                        break;
                    }
                } else {
                    // 查询失败需要重新发送查询请求
                    sllep = sllep + 1;
                    Thread.sleep(sllep * 1000);
                    logger.info("代付" + "UnionPay1517535562097H4W6Jj0" + "查询交易应答码=【" + retQueryMap.get("respCode")
                            + "#" + retQueryMap.get("respMsg") + "】，需要继续查询交易状态");

                }
            } catch (InterruptedException e) {
            }
        }
    }

}
