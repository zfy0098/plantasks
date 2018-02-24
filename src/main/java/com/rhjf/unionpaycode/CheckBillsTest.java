package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.MrAzuSplitUtil;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hadoop on 2018/1/9.
 *
 * @author hadoop
 */
public class CheckBillsTest extends OnlineBaseDao {


    private static final String CHANNEL_ID = "UNIONPAYQRCODE";


    private String date ;

    public CheckBillsTest(String date){
        this.date = date;
    }

    public void init() {


        String fileDate = date.replace("-" , "").substring(2);


        String filePath = "/opt/sh/unionpay/duizhang_file/RD2007" +fileDate + "00";


        log.info("获取 yyMMdd 格式的时间:" + fileDate);
        log.info("获取文件路径：" + filePath);
        log.info("开始执行银联二维码对账 ， 日期：" + date);


        List<String> fileList = new ArrayList<>();

        String tempLine;
        try {
            // 拼接文件路径
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"));
            // 读取文件
            for (int i = 0; (tempLine = br.readLine()) != null; i++) {
                fileList.add(tempLine);
            }
            br.close();
        } catch (IOException e) {
            log.info("读取文件失败 , " + e.getMessage());
            log.error(e);
            System.exit(1);
            return ;
        }

        log.info("========进入文件导入======");
        StringBuffer sbs = new StringBuffer();
        for (String item : fileList) {
            sbs.append(item + "\n");
        }
        String cof = sbs.toString();





        InputStream inputStream;
        Integer insertNum = 0;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sfd = new SimpleDateFormat("yyyyMMdd");

        try {
            inputStream = new ByteArrayInputStream(cof.getBytes("GBK"));
            String str = MrAzuSplitUtil.readFileByChars(inputStream, "GBK");


            String[] dataBlock = str.split("==============================");

            log.info("dataBlock.length" + dataBlock.length);


            Map<String,Object> map = new HashMap<String,Object>();

            Map<String,Integer> countMap = new HashMap<String,Integer>();


            for (int j = 0; j < dataBlock.length; j++) {
                if (dataBlock[j] == null || dataBlock[j].length() == 0) {
                    continue;
                }
                String[] gn = dataBlock[j].split("\n");
                //商户编号
                StringBuffer sbTraderCode = new StringBuffer();
                ///商户名称
                StringBuffer sbTraderName = new StringBuffer();
                //清算日期
                StringBuffer sbliqiDate = new StringBuffer();
                //生成日期
                StringBuffer sbcreaDate = new StringBuffer();
                //当前年
                String thisYear = new SimpleDateFormat("yyyy").format(new Date());
                //交易日期转码
                SimpleDateFormat sdfForTranDate = new SimpleDateFormat("yyyyMMddHHmmss");
                //交易日期年月日
                SimpleDateFormat sdfForTranDateToDate = new SimpleDateFormat("yyyy-MM-dd");
                //交易日期时分秒
                SimpleDateFormat sdfForTranDateToTime = new SimpleDateFormat("HH:mm:ss");
                log.info("第" + j + "个数据块的的条数" + gn.length);


                for (int k = 2; k < gn.length; ) {
                    int flag = 0;
                    switch (k) {
                        case 3:
                            //获取无空格条目
                            String strOne = MrAzuSplitUtil.splitForString(gn[k], " ");
                            String[] strTwo = strOne.split("商户名称：");
                            sbTraderCode.append(strTwo[0].replace("商户编号：", ""));

                            sbTraderName.append(strTwo[1].split("收单机构：")[0]);

                            String sql = "select * from TBL_PAY_MERCHANTCHANNEL where channelCode='UNIONPAYQRCODE' and superMerchantNo=?";
                            List<Map<String, Object>> mList = queryForList(sql, new Object[]{sbTraderCode.toString().trim()});

                            String merchantChannelConfigSql = "SELECT * from TBL_PAY_MERCHANTCHANNELCONFIG where superMerchantNo=?";
                            Map<String, Object> mcc = queryForMap(merchantChannelConfigSql, new Object[]{sbTraderCode.toString().trim()});


                            if (mList.size() == 0 && mcc == null) {
                                flag = 1;
                                log.info("商户号：" + sbTraderCode.toString() + "非银联二维码上游商户号，跳过");
                            }
                            k++;
                            break;
                        case 5:
                            //获取无空格条目
                            String strThree = MrAzuSplitUtil.splitForString(gn[k], " ");
                            String[] strFour = strThree.split("生成日期：");
                            sbliqiDate.append(strFour[0].replace("清算日期：", ""));
                            sbcreaDate.append(strFour[1]);
                            k++;
                            continue;
                        case 12:
                            do {
                                if ("".equals(gn[k].trim())) {
                                    break;
                                }
                                String strSix = MrAzuSplitUtil.splitForStringByCondition(gn[k], " ", "@@@", 1);
                                String[] strFive = strSix.split("@@@");

                                if ("消费撤消".equals(strFive[11]) || strFive[11].startsWith("差错")) {
                                    k++;
                                    continue;
                                }

                                String transDate = strFive[1];
                                String before = thisYear + "-" + transDate.substring(0, 4);
                                String time = transDate.substring(4, 10);
                                StringBuilder sb = new StringBuilder(time);
                                StringBuilder sbDate = new StringBuilder(before);

                                String datetime = sbDate.insert(7, "-").toString() + " " + sb.insert(2, ":").insert(5, ":").toString();

                                String cardNo = strFive[2];
                                String cardType = strFive[3];
                                String bankName = strFive[4];

                                Object[] obj = new Object[]{strFive[8], new BigDecimal(strFive[5]).setScale(2, BigDecimal.ROUND_HALF_UP),
                                        datetime, new BigDecimal(strFive[6].substring(1)).setScale(2, BigDecimal.ROUND_HALF_UP), CHANNEL_ID, date,cardNo , cardType, bankName};


                                String amount = map.get(strFive[3]) == null ? "0" : map.get(strFive[3]).toString();

                                Integer count = countMap.get(strFive[3]) == null ? 0 : countMap.get(strFive[3]);

                                Double trxAmount = AmountUtil.add(amount , strFive[5]);

                                map.put(strFive[3] , trxAmount);


                                count += 1;

                                countMap.put(strFive[3] , count);


                                k++;
                                continue;
                            } while (k < gn.length);
                            break;
                        default:
                            k++;
                            continue;
                    }
                    if (flag == 1) {
                        break;
                    }
                }
            }

            log.info("执行到最后打印map：");
            System.out.println(map.toString() + "----------------" + countMap  + "---------" + date);
        } catch (Exception e) {
            log.info(e);
            System.exit(1);
            return ;

        }
    }




    public static void main(String[] args) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE , -1);
        String date = sdf.format(c.getTime());

        if(args.length > 0 ){
            date = args[0];
        }

        date = "20180114";
        CheckBillsTest checkBills = new CheckBillsTest(date);
        // 读取unionpay 对账文件
        checkBills.init();

    }
}
