package com.jiuxiu.yxsdk;

import com.utils.HttpClient;
import org.apache.xmlbeans.impl.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiTest {



    private static final String URL = "http://localhost:9090/usercenter/";


    public void login(){

        Map<String , Object> map = new HashMap<>();
        map.put("unionid" , "oCCRds8deTJl3MHYOBJkPbPdpm9w");
        map.put("accesstoken" , "accesstoken");
        map.put("source" , "weixin");
        map.put("appid" ,  1001);
        map.put("channel_id" , "1");
        map.put("sdk_ver" , "12");
        map.put("child_id" , "1");
        map.put("sign"  , "230CE1D9E4A7C897F318A2FE80964530");
        map.put("t" , 1524133476);

        System.out.print(URL + "login/other");

        String result = HttpClient.post(URL + "login/other"  , map , "1");
        System.out.print(result);
    }


    public void createOrder(){
        String url = "http://localhost:8080/usercenter/cp_pay/create_order?actoken=123";
        Map<String,Object> map = new HashMap<>(16);
        map.put("amount" , 1);
        String cp_order_no = UUID.randomUUID().toString();
        map.put("cp_order_no" , cp_order_no);
        map.put("currency" , "CNY");
        map.put("appid" , "1000");
        map.put("child_id" , "1111");
        map.put("imei" , 1);
        map.put("server_id" , 1);
        map.put("product_id" , 1);
        map.put("payment_type" , 1);
        map.put("sign" , 1);
        map.put("t" , Integer.parseInt(System.currentTimeMillis()/1000+""));


        String result = HttpClient.post(url , map , "1");

        System.out.print(result);
    }


    /**
     * 加密
     *
     * @param content
     * @param passwd
     * @return
     */
    public static String aesEncrypt(String content, String passwd) {
        try {
            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(passwd.getBytes(), "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = aesECB.doFinal(content.getBytes());
            String str = new String(Base64.encode(result));
            return str.replace("+", "=jia=");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String aesDecrypt(String content , String passwd){
        try {
            content = content.replace("=jia=", "+");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(passwd.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = Base64.decode(content.getBytes());
            return new String(cipher.doFinal(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void register(){

        long t = System.currentTimeMillis();

        System.out.print(t);

        String key = "23094b343e52485b4fbf9d94a8bc55a5".substring(10, 26);
        String pwdContent = aesEncrypt("1234567#1001#1#1#1524224168" , key);

        Map<String , Object> map = new HashMap<>();
        map.put("phone" , "13159949877");
        map.put("password" , pwdContent);
        map.put("code" , "888888");
        map.put("appid" ,  1001);
        map.put("child_id" , "1");
        map.put("channel_id" , 1);

        map.put("os" , "1");
        map.put("sdk_ver" , "12");

        map.put("sign"  , "D3B383116AB1FFD67A2F776F31976464");
        map.put("t" , 1524224168);

        System.out.print(URL + "login/other");

        String result = HttpClient.post(URL + "register/phone"  , map , "1");
        System.out.print(result);
    }




    public static void main(String[] args){
//        String key = "23094b343e52485b4fbf9d94a8bc55a5".substring(10, 26);
//
//        String password = ApiTest.aesDecrypt("4F8C6786338F10544BB8C79192DDCE13" , key);
//        System.out.println(password);


        ApiTest apiTest = new ApiTest();
        apiTest.register();

    }

}
