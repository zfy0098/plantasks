package com.jiuxiu.spark;

import net.sf.json.JSONArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IDEA by Zhoufy on 2018/5/10.
 *
 * @author Zhoufy
 */
public class SparkConfiguration {

    private static Logger log = Logger.getLogger(SparkConfiguration.class);

    static final String ZK_QUORUM = "192.168.0.247:9092";
    static final String GROUP = "test-consumer-group";

    static final String TOP = "HelloWorld";

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("spark://192.168.0.247:7077").setAppName("streaming word count");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.seconds(5));

        Collection<String> topicsSet = new HashSet<>(Arrays.asList(TOP.split(",")));

        //kafka相关参数，必要！缺了会报错
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", ZK_QUORUM);
        kafkaParams.put("group.id", GROUP);
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("enable.auto.commit", false);


        //通过KafkaUtils.createDirectStream(...)获得kafka数据，kafka相关参数由kafkaParams指定
        JavaInputDStream<ConsumerRecord<String, String>> lines = KafkaUtils.createDirectStream(
                ssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicsSet, kafkaParams)
        );

        log.info("lines.flatMap ============>");
        System.out.println("lines.flatMap ============>");

        JavaPairDStream<String, Integer> counts = lines.flatMap(new FlatMapFunction<ConsumerRecord<String, String>, String>() {

            @Override
            public Iterator<String> call(ConsumerRecord<String, String> stringStringConsumerRecord) throws Exception {

                log.info("获取  offset : " + stringStringConsumerRecord.offset());


                OffsetRange[] offsetRanges = new OffsetRange[1];

                System.out.println("获取  offset : " + stringStringConsumerRecord.offset());

                return Arrays.asList(stringStringConsumerRecord.value().split("#")).iterator();
            }
        }).mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s, 1);
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });


        log.info("count.foreachRDD ===== >");
        System.out.println("count.foreachRDD ===== >");

        counts.foreachRDD(new VoidFunction<JavaPairRDD<String, Integer>>() {
            @Override
            public void call(JavaPairRDD<String, Integer> stringIntegerJavaPairRDD) throws Exception {
                stringIntegerJavaPairRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
                    @Override
                    public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        log.info("````````````````````````````````" + stringIntegerTuple2._1 + "--------------------------" + stringIntegerTuple2._2);
                        System.out.println("````````````````````````````````" + stringIntegerTuple2._1 + "--------------------------" + stringIntegerTuple2._2);
                    }
                });
            }
        });

        log.info(" lines .foreachrdd ===== >");
        System.out.println(" lines .foreachrdd ===== >");
        lines.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, String>>>() {
            @Override
            public void call(JavaRDD<ConsumerRecord<String, String>> rdd) {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();

                rdd.foreach(x -> {
                    System.out.println(x.key() + "---------------" + x.value());
                });

                log.info("offsetRanges :  " + JSONArray.fromObject(offsetRanges).toString());

                for (int i = 0; i < offsetRanges.length; i++) {
                    System.out.println("输出 offsetRanges ：" + offsetRanges[i]);
                }
                ((CanCommitOffsets) lines.inputDStream()).commitAsync(offsetRanges);
            }
        });

        ssc.start();
        ssc.awaitTermination();
    }


}
