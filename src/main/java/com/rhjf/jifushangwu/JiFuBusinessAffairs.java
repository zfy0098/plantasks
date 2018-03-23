package com.rhjf.jifushangwu;

import com.rhjf.utils.DateUtil;
import com.rhjf.utils.HttpClient;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by hadoop on 2018/3/15.
 *
 * @author hadoop
 */
public class JiFuBusinessAffairs {

    private Logger log = Logger.getLogger(this.getClass());

    private String url = "http://fast.jfpays.com:19085/rest/api/";

    private String merchantNo = "0001YSJF";

    private String aesKey = "0000000000000000";

    private String signKey = "0000000000000000";

    private String nowDate = DateUtil.getNowTime(DateUtil.yyyyMMdd);

    private String nowTime = DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss);


    public void init() {

        JSONObject head = new JSONObject();

        String txnCode = "312001";

        ThreadLocalRandom random = ThreadLocalRandom.current();

        String orderID = nowTime + random.nextInt(10000000, 99999999);

        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo", merchantNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);
        head.put("orderId", orderID);
        head.put("reqDate", nowDate);
        head.put("reqTime", nowTime);

        log.info("head :" + head.toString());

        JSONObject json = new JSONObject();

        String mobile = "18611769740";
        String IdCard = "130823199105284010";
        String cvv2 = "649";
        String calidity = "0819";
        String cardHolderName = "张志国";

        String payBankCardNo = "6225760013991968";
        String inBankCardNo = "6214920207968049";

        json.put("head", head);
        json.put("CutDate", nowDate);
        json.put("NotifyUrl", "http://111.207.6.230:8085/web/trxnotify.html");
        json.put("TxnAmt", 50000);
        json.put("t0TxnRate", "0.0036");
        json.put("t0TxnFee", 0);
        json.put("cardHolderName", cardHolderName);
        json.put("Mobile", mobile);
        json.put("IdCard", IdCard);
        json.put("cvv2", cvv2);
        json.put("Validity", calidity);
        json.put("payBankCardNo", payBankCardNo);

        json.put("inBankCardNo", inBankCardNo);
        json.put("payBankCode", "308");
        json.put("inBankCode", "303");
        json.put("Remark", "闪开，我是测试的");

        log.info("加密原文：" + json.toString());

        String plainText = null;
        try {
            plainText = encrypt(json.toString(), aesKey.getBytes(), aesKey.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("aes 加密结果:" + plainText);

        String encryptData = getSha1(json.toString() + signKey);

        log.info("sha1 结果：" + encryptData);

        Map<String, Object> map = new HashMap<>(16);

        map.put("encryptData", plainText);
        map.put("partnerNo", merchantNo);
        map.put("signData", encryptData);
        map.put("orderId", orderID);
        map.put("ext", "闪开，我是测试的");

        log.info("请求报文“： " + JSONObject.fromObject(map).toString());

        String content = null;
        try {
            log.info("请求地址：" + url + txnCode);
            content = HttpClient.post(url + txnCode, null, map, "1");
            log.info("请求返回报文：" + content);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        JSONObject resultJSON = JSONObject.fromObject(content);

        String resultEncryptData = resultJSON.getString("encryptData");
        String signature = resultJSON.getString("signature");

        String text = decrypt(resultEncryptData, aesKey.getBytes(), aesKey.getBytes());
        String resultSign = getSha1(text + signKey);

        log.info("响应报文明文：" + text);
        log.info("响应签名：" + signature + " , 计算签名：" + resultSign);

        resultJSON = JSONObject.fromObject(text);

        head = JSONObject.fromObject(resultJSON.getString("head"));
        String respCode = head.getString("respCode");

        if ("000000".equals(respCode)) {
            log.info("订单：" + orderID + " ， 成功");
            String workId = head.getString("workId");

            confirmOrder(orderID, workId);
        }
    }


    public void confirmOrder(String orderID, String workId) {

        JSONObject head = new JSONObject();

        String txnCode = "312002";

        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo", merchantNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);
        head.put("orderId", orderID);
        head.put("reqDate", nowDate);
        head.put("reqTime", nowTime);

        log.info("head :" + head.toString());

        JSONObject json = new JSONObject();

        json.put("head", head);
        json.put("workId", workId);
        Scanner in = new Scanner(System.in);

        System.out.print("请输入短信验证码:");
        String smsCode = in.nextLine();

        log.info("获取短信验证码为：" + smsCode);

        if (smsCode == null || smsCode.trim().equals("")) {
            log.info("短信验证码获取失败");
            System.exit(1);
        }

        json.put("smsCode", smsCode);
        log.info("加密原文：" + json.toString());


        String plainText = null;
        try {
            plainText = encrypt(json.toString(), aesKey.getBytes(), aesKey.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("aes 加密结果:" + plainText);

        String encryptData = getSha1(json.toString() + signKey);

        log.info("sha1 结果：" + encryptData);

        Map<String, Object> map = new HashMap<>(16);

        map.put("encryptData", plainText);
        map.put("partnerNo", merchantNo);
        map.put("signData", encryptData);
        map.put("orderId", orderID);
        map.put("ext", "闪开，我是测试的");

        log.info("请求报文“： " + JSONObject.fromObject(map).toString());

        String content = null;
        try {
            log.info("请求地址：" + url + txnCode);

            content = HttpClient.post(url + txnCode, null, map, "1");
            log.info("支付返回报文：" + content);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        JSONObject resultJSON = JSONObject.fromObject(content);

        String resultEncryptData = resultJSON.getString("encryptData");
        String signature = resultJSON.getString("signature");

        String text = decrypt(resultEncryptData, aesKey.getBytes(), aesKey.getBytes());
        String resultSign = getSha1(text + signKey);

        log.info("响应报文明文：" + text);
        log.info("响应签名：" + signature + " , 计算签名：" + resultSign);

//        selectOrder(orderID , workId);

    }


    public void selectOrder(String orderID, String workId) {

        /**
         *  查询返回报文：{"signature":"76e636aaaf957670cbd13b275f7b71b8ca805980","encryptData":"zM4H2W1kYvmw9i5IrM6Yf81FRA3pCDE33uTgxKABBz9xTvuCP9RFtC3SvA1zoK4zGsm4vTc1CoCvXLnQmEsdMmymUpL2Q8npoOyn23D4RAKIevbtZXlDbIklgOfcc5WKAaFdyxHDXza67q38gHFXVVrwZn/laELr1l/S1+i1E6SY/4WD1/2PIpDQJsoevXUC4Ws7N4xNcvwtoKwgVuW3fKSXXQlQ2yChqn3CnV1hQP+eV6GIBr05Im2lDZCwZJb7Nd1sVCzzXvyFZulmzl1hdffvE/wIBbKkRbH9TtON+KaVxfk4NaJWyo/A2oY8HRw0TWM8OFSsg21UJiSI6nyqt451h85aZpxDIBpYN93VRrAqf0WgXByeZQ2sqhEWItayySSZKwKiqxHOLnqn0JsoKGYeW0zJxPpVdV88TCB6ChDBXVYuq5vTbIJk8Qme+cG/c9wZvWimZYJDMwR9d7Bbvw=="}
         *  响应报文明文：{"head":{"charset":"UTF-8","orderId":"2018031616333356165480","partnerNo":"0001YSJF","partnerType":"OUTER","respCode":"000000","respDate":"20180316","respMsg":"success","respTime":"20180316163601","txnCode":"312003","version":"1.0.0","workId":"974564320552681473"},"orderDesc":"支付成功","orderStatus":"01","workId":"974564320552681473"}
         *
         */

        JSONObject head = new JSONObject();

        String txnCode = "312003";

        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo", merchantNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);
        head.put("orderId", orderID);
        head.put("reqDate", nowDate);
        head.put("reqTime", nowTime);

        log.info("head :" + head.toString());

        JSONObject json = new JSONObject();

        json.put("head", head);
        json.put("workId", workId);
        json.put("orderId", orderID);

        String plainText = null;
        try {
            plainText = encrypt(json.toString(), aesKey.getBytes(), aesKey.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("aes 加密结果:" + plainText);

        String encryptData = getSha1(json.toString() + signKey);

        log.info("sha1 结果：" + encryptData);

        Map<String, Object> map = new HashMap<>(16);

        map.put("encryptData", plainText);
        map.put("partnerNo", merchantNo);
        map.put("signData", encryptData);
        map.put("orderId", orderID);
        map.put("ext", "闪开，我是测试的");

        log.info("请求报文“： " + JSONObject.fromObject(map).toString());

        String content = null;
        try {
            log.info("请求地址：" + url + txnCode);
            content = HttpClient.post(url + txnCode, null, map, "1");
            log.info("查询返回报文：" + content);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        JSONObject resultJSON = JSONObject.fromObject(content);

        String resultEncryptData = resultJSON.getString("encryptData");
        String signature = resultJSON.getString("signature");

        String text = decrypt(resultEncryptData, aesKey.getBytes(), aesKey.getBytes());
        String resultSign = getSha1(text + signKey);

        log.info("响应报文明文：" + text);
        log.info("响应签名：" + signature + " , 计算签名：" + resultSign);

    }


    public void trxNotify() {

        String encryptData = "zM4H2W1kYvmw9i5IrM6Yf81FRA3pCDE33uTgxKABBz8s8jsn8uzqiJEej2YQNS9rx64jcsrthlEPJCbovyu3VEV5qxqnd+CVLUILGydSXKM+3vO1WRBozp9Bt3Q3R76g4RYEusNF3gjWyPgI5IrkYRN0DmpfH03B+bryMq0CyMva71mU9Z5jt55UJmZWSopYqdrzzYXGTdm9KUm14xv9jygWbFxCpZr0Sl9VjZhQcddQ6WjvajVrVKAmJBb45yTGjA8ReijeJI91HnDUHG8XfRq8HfLL0rut+HgB2dPvy9EU887vktjYbUPYMSSeAmWJnd86dSPeC6bEnR9RSpGWQ2TabHcSsFgUVvmOm7HvGYQbPjOYN5NMyohzw/OT/V+rY5CY2l5Jt+G7phQmv1enqYcs0dp/6k9ZBflHJ6Dxcy3q+9ER7OlY7TvBxCJheeQy7RDCn708Let2hFROHoeXr37iQFYBXsrAmPmpqE9sg34=";
        String signature = "a4351224ab8fb9d4e9092f9430df7cfcf51038f4";

        String plainText = decrypt(encryptData, aesKey.getBytes(), aesKey.getBytes());

        log.info("接收回调的明文 : " + plainText);

        String sign = getSha1(encryptData + signKey);

        log.info(sign + "====" + signature);



        JSONObject plainTextJSON = JSONObject.fromObject(plainText);

        JSONObject head = JSONObject.fromObject(plainTextJSON.getString("head"));

        String orderID = head.getString("orderId");








        if ("000000".equals(head.getString("respCode")) && "01".equals(plainTextJSON.getString("orderStatus"))) {
            log.info("支付成功 , workId(上游平台流水号) :" + head.getString("workId"));
        }
    }


    public static void main(String[] args) {
        JiFuBusinessAffairs jifu = new JiFuBusinessAffairs();
//        jifu.init();
//        jifu.selectOrder("2018031617111524895156" , "974573807690842113");
        jifu.trxNotify();
    }


    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }


    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 数据加密
     *
     * @param srcData
     * @param key
     * @param iv
     * @return
     */
    public String encrypt(String srcData, byte[] key, byte[] iv) {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher;
        String encodeBase64String = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            SecureRandom rnd = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            byte[] encData = cipher.doFinal(srcData.getBytes());
            return Base64.encodeBase64String(encData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数据解密
     *
     * @param encDataStr
     * @param key
     * @param iv
     * @return
     */
    public String decrypt(String encDataStr, byte[] key, byte[] iv) {
        byte[] encData = Base64.decodeBase64(encDataStr);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher;
        byte[] decbbdt = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            SecureRandom rnd = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            decbbdt = cipher.doFinal(encData);
            return new String(decbbdt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
