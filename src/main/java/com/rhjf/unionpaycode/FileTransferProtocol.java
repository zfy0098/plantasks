package com.rhjf.unionpaycode;

import com.rhjf.base.OnlineBaseDao;
import com.rhjf.email.SendMail;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 2018/3/6.
 *
 * @author hadoop
 */
public class FileTransferProtocol extends OnlineBaseDao {


    public FileTransferProtocol(String date) {

        log.info("开始为代理商生成对账文件, 交易日期:" + date);


        String path = "/opt/sh/unionpay/agent/" + date + "/";

        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }

        String agentIdSql = "SELECT tbm.agent_id , toc.merchantNo from tbl_pay_merchant as tbm INNER JOIN " +
                " (SELECT merchantNo FROM tbl_online_copyorder  WHERE  channelId='UNIONPAYQRCODE'  AND  date(withdrawTime)=? GROUP BY merchantNo) as toc" +
                "  on tbm.merchantNo=toc.merchantNo ";

        List<Map<String, Object>> agentIDList = queryForList(agentIdSql, new Object[]{date});

        Map<String, List<String>> map = new HashMap<>();

        for (int i = 0; i < agentIDList.size(); i++) {
            String agent_id = agentIDList.get(i).get("agent_id").toString();

            List<String> list = map.get(agent_id);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(agentIDList.get(i).get("merchantNo").toString());
            map.put(agent_id, list);
        }

        log.info(JSONObject.fromObject(map).toString());

        int threadCount = map.keySet().size();

        ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(threadCount);

        for (String key : map.keySet()) {

            List<String> merchantNoList = map.get(key);

            newScheduledThreadPool.schedule(new TimerFile(merchantNoList, date, key, path), 1, TimeUnit.MILLISECONDS);
        }
        newScheduledThreadPool.shutdown();

        log.info("生成txt文件结束");
    }


    public void foundTXT(String fileName, String content) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.write(content.getBytes());
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TimerFile implements Runnable {

        private List<String> merchantNoList;
        private String date;
        private String key;
        private String path;

        public TimerFile(List<String> merchantNoList, String date, String key, String path) {
            this.merchantNoList = merchantNoList;
            this.date = date;
            this.key = key;
            this.path = path;
        }

        @Override
        public void run() {

            String sql = "SELECT merchantNo , orderNumber , orderAmount , receiverFee , createDate  FROM tbl_online_copyorder " +
                    " WHERE  date(withdrawTime)='" + date + "' and channelId='UNIONPAYQRCODE' AND checkScanCodeStatus='SUCCESS' and " +
                    " merchantNo in ";

            StringBuffer sbf = new StringBuffer("(");

            for (int i = 0; i < merchantNoList.size(); i++) {
                if (i != 0 && i != merchantNoList.size()) {
                    sbf.append(",");
                }
                sbf.append("'").append(merchantNoList.get(i)).append("'");
            }
            sbf.append(")");
            String newSql = sql + sbf.toString();

            List<Map<String, String>> orderList = queryForList(newSql);

            log.info("sql ： " + newSql + " , orderList.size()  :" + orderList.size());

            String[] title = {"商户号", "订单号", "交易金额", "手续费", "创建时间"};

            List<Object[]> orderListObj = new ArrayList<>(orderList.size());

            StringBuffer content = new StringBuffer("商户号|订单号|交易金额|手续费|创建时间" + "\r\n");

            for (int i = 0; i < orderList.size(); i++) {
                Map<String, String> orderMap = orderList.get(i);

                content.append(orderMap.get("merchantNo") + "|" + orderMap.get("orderNumber") + "|" + orderMap.get("orderAmount") + "|" +
                        orderMap.get("receiverFee") + "|" + orderMap.get("createDate") + "\r\n");
            }

            foundTXT(path + key + ".txt", content.toString());

            if ("130".equals(key)) {
                SendMail.sendMail("交易", "交易", new String[]{"zhoufangyu@ronghuijinfubj.com", "zhangzhiguo@ronghuijinfubj.com"},
                        null, new String[]{path + key + ".txt"});
            }
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        String date = sdf.format(c.getTime());

        if (args.length > 0) {
            date = args[0];
        }

        new FileTransferProtocol(date);
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000.0);
    }
}
