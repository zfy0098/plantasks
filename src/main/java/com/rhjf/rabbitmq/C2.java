package com.rhjf.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Created by hadoop on 2018/2/27.
 *
 * @author hadoop
 */
public class C2 {

    private final static String queueName = "queue_test";


    public static void main(String[] args) throws Exception{

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.16.1.90");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName ,  true , false , false , null);

        while(true){

            Thread.sleep(10);

            GetResponse response = channel.basicGet(queueName , false);

            if(response != null){
                AMQP.BasicProperties props = response.getProps();
                byte[] body = response.getBody();
                long deliveryTag = response.getEnvelope().getDeliveryTag();

                String message = new String(body);
                System.out.println("收到消息'" + message + "'");
                channel.basicAck(deliveryTag, true);
            }
        }


        /* 创建消费者对象，用于读取消息 */
//        QueueingConsumer consumer = new QueueingConsumer(channel);
//        channel.basicConsume(queueName, true, consumer);

        /* 读取队列，并且阻塞，即在读到消息之前在这里阻塞，直到等到消息，完成消息的阅读后，继续阻塞循环 */
//        while (true) {
//
//            Thread.sleep(1000);
//
//            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//            String message = new String(delivery.getBody());
//            System.out.println("收到消息'" + message + "'");
//        }

    }
}
