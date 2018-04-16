package com.rhjf;

import com.rhjf.utils.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hadoop on 2018/3/29.
 *
 * @author hadoop
 */
public class AddressCaseTest {


    public static void main(String[] args){

        String url = "http://www.gpsspg.com/apis/maps/geo/";

        Map<String,Object> head = new HashMap<>(16);

        head.put("Host" , "www.gpsspg.com");
        head.put("Connection" , "keep-alive");
        head.put("Accept" , "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
        head.put("X-Requested-With" , "XMLHttpRequest");
        head.put("User-Agent" , "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        head.put("Referer" , "http://www.gpsspg.com/iframe/maps/google_180314.htm?mapi=0");
        head.put("Accept-Encoding" , "gzip, deflate");
        head.put("Accept-Language" , "zh-CN,zh;q=0.9");
        head.put("Cookie" , "ARRAffinity=ce31724d0b10e7678a240b9eb2d013216e04c88d01083b878c5c18b239e00810; Hm_lvt_15b1a40a8d25f43208adae1c1e12a514=1522311981; __51cke__=; Hm_lpvt_15b1a40a8d25f43208adae1c1e12a514=1522312143; __tins__540082=%7B%22sid%22%3A%201522311981087%2C%20%22vd%22%3A%203%2C%20%22expires%22%3A%201522313943184%7D; __51laig__=3");

//       ,130.2978150228
        Map<String,Object> map = new HashMap<>(16);

        map.put("output" , "jsonp");
        map.put("lat" , "47.3313529056");
        map.put("lng" , "130.2978150228");
        map.put("type" , 0);
        map.put("callback" , "jQuery110200763476904424003_1522312122229");
        map.put("_" , "1522312122242");


        try {
            String content = HttpClient.post(url , head , map , "1");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
