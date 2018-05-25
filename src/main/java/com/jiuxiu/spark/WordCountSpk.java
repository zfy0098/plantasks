package com.jiuxiu.spark;


import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * Created with IDEA by ChouFy on 2018/5/4.
 *
 * @author Zhoufy
 */
public class WordCountSpk {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf();

        conf.setAppName("WordCounter").setMaster("spark://192.168.0.247:7077");

        String fileName = "/new4.txt";

        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile(fileName, 1);

        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            private static final long serialVersionUID = 1L;

            // 以前的版本好像是Iterable而不是Iterator
            @Override
            public Iterator<String> call(String line) throws Exception {
                return Arrays.asList(strToArray(line)).iterator();
            }
        });

        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction< String, String, Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });

        JavaPairRDD<String, Integer> result = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Integer call(Integer e, Integer acc)
                    throws Exception {
                return e + acc;
            }
        }, 1);

        result.map(
                new Function<Tuple2<String, Integer>, Tuple2<String, Integer>>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Integer> call(Tuple2<String, Integer> v1) throws Exception {
                        return new Tuple2<>(v1._1, v1._2);
                    }
                }).sortBy(new Function<Tuple2<String, Integer>, Integer>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Integer call(Tuple2<String, Integer> v1)
                            throws Exception {
                        return v1._2;
                    }
                }, false, 1).foreach(new VoidFunction<Tuple2<String, Integer>>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void call(Tuple2<String, Integer> e)throws Exception {
                        System.out.println("【" + e._1 + "】出现了" + e._2 + "次");
                    }
                });
        sc.close();

    }


    public static String[] strToArray(String content){
        String[] stringArray = new String[content.length()];
        for (int i = 0; i < content.length(); i++) {
            stringArray[i] = String.valueOf(content.charAt(i));
        }
        return stringArray;
    }
}
