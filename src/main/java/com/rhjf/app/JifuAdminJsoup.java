package com.rhjf.app;

import com.rhjf.utils.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/3/26.
 *
 * @author hadoop
 */
public class JifuAdminJsoup {


    public void init(){
        String url = "https://admin.jfpays.com/kj/tshortlogclearingrecordcmbcah/exportExcel?token=6c8b61dfb763bab4577216f08f95f220&cutDate=2018-03-25&cutDateEnd=2018-03-25&orgCode=&platMerchantCode=&status=&partnerNo=";


        Map<String,Object> map = new HashMap<>(16);
        map.put("token" , "c20c6d00fe35cc55782bc2fccb65df6e");
        map.put("cutDate" , "2018-03-25");
        map.put("cutDateEnd" , "2018-03-25");
        map.put("orgCode" , "");
        map.put("platMerchantCode" , "");
        map.put("status" , "");
        map.put("partnerNo" , "");

        HttpGet getMethod = new HttpGet(url);

        org.apache.http.client.HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(null).build();


        getMethod.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        getMethod.addHeader("CONNECT" , "admin.jfpays.com:443 HTTP/1.1");



        try {

//            List<NameValuePair> formparams = new ArrayList<>();
//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
//            }


            // 执行getMethod
            HttpResponse response = httpClient.execute(getMethod);

            HttpEntity rspentity = response.getEntity();
            InputStream in = rspentity.getContent();

            OutputStream outStream = new FileOutputStream("e:/44444.csv");
            IOUtils.copy(in, outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放连接
            getMethod.releaseConnection();
        }



        try {
            Document doc = Jsoup.connect("https://admin.jfpays.com/kj/tshortlogclearingrecordcmbcah/exportExcel?token=6c8b61dfb763bab4577216f08f95f220&cutDate=2018-03-25&cutDateEnd=2018-03-25&orgCode=&platMerchantCode=&status=&partnerNo=")
//                    .data("username" , "Kj_HTu3kLl0")
//                    .data("password" , "rhjf$123456")
//                    .data("captcha" , "")
//                    .data("status" ,"")
                    .get();


            System.out.println(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String [] args){
        JifuAdminJsoup jifu = new JifuAdminJsoup();
        jifu.init();
    }
}
