package com.rhjf.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hadoop on 2018/2/27.  *  * @author hadoop
 */
public class P {
    private final static String queueName = "queue_test";
    private final static String exchange = "exchange_test";
    private static int count = 0;
    private Lock lock = new ReentrantLock(true);

    public void sendMQ() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.16.1.90");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");
        factory.setPort(5672);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchange, "test");
        count += 1;
        channel.basicPublish(exchange, "test", null, String.valueOf(count).getBytes());
        channel.close();
        connection.close();
    }

    public class Task implements Runnable {
        private CyclicBarrier cyclicBarrier;

        public Task(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
                int count = 1;
                for (int i = 0; i < count; i++) {
                    sendMQ();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        int threadCount = 19000;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        ExecutorService executorService = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(new P().new Task(cyclicBarrier));
        }
        executorService.shutdown();
        Long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000.0);
        //        while (!executorService.isTerminated()) {
        //            try {
        //                Thread.sleep(10);
        //            } catch (InterruptedException e) {
        //                e.printStackTrace();
        //            }
    }
}