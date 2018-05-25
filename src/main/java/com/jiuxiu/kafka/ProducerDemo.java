package com.jiuxiu.kafka;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IDEA by Zhoufy on 2018/5/2.
 *
 * @author Zhoufy
 */
public class ProducerDemo {

    public void init(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.0.247:9092");
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("batch.size", 16384);
        properties.put("linger.ms", 1);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(properties);
        try {

            ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
            ExecutorService executorService = new ThreadPoolExecutor(10 , 10 , 0L , TimeUnit.MILLISECONDS ,
                    new LinkedBlockingDeque<>() , threadFactory , new ThreadPoolExecutor.AbortPolicy());


            String imei = UUID.randomUUID().toString();
            String idfa = UUID.randomUUID().toString();
            for (int i = 0; i < 1; i++) {
                executorService.execute(new ProducerThread(producer , "" , ""));
            }
            executorService.shutdown();

            while (true){
                if(executorService.isTerminated()){
                    producer.close();
                    break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        ProducerDemo producerDemo = new ProducerDemo();
        producerDemo.init();

    }



    private class ProducerThread implements Runnable{

        Producer<String, String> producer;

        String imei ;

        String idfa;


        public ProducerThread(Producer<String, String> producer , String imei , String idfa){
            this.producer = producer;
            this.imei = imei;
            this.idfa = idfa;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                JSONObject json = new JSONObject();
                json.put("device_os_ver" , "");
                json.put("imei" , i);
                json.put("sign" , "");
                json.put("logid" , idfa);
                json.put("api_ver" , "");
                json.put("mac" , "");
                json.put("package_id", 0 );
                json.put("child_id" , 1);
                json.put("device_name" , "");
                json.put("app_channel_id" , 0);
                json.put("os" , 1);
                json.put("channel_id" , 0);
                json.put("timestamp" , System.currentTimeMillis()/1000);
                json.put("sdk_ver" ,"");
                json.put("idfa" , UUID.randomUUID().toString());
                json.put("appid" , 10033);
                json.put("client_ip" , "127.0.0.1" );

                producer.send(new ProducerRecord<String, String>("device_install", json.toString()));
                System.out.println("Sent:" + json);
            }
        }
    }
}
