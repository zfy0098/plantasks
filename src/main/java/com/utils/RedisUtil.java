package com.utils;

import java.util.HashMap; 
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class RedisUtil {

	public static void main(String[] args) {
		// 连接本地的 Redis 服务
		// localhost:服务器地址 , redis端口号
		Jedis jedis = new Jedis("localhost", 6379);
		jedis.auth("zhoufangyu");
		System.out.println("Connection to server sucessfully");
		// 设置 redis 字符串数据
		jedis.set("name", "zfy");
		System.out.println("redis key is name , values is " + jedis.get("name"));

		// 存储数据到列表中
		jedis.lpush("tutorial-list", "Redis");
		jedis.lpush("tutorial-list", "Mongodb");
		jedis.lpush("tutorial-list", "Mysql");
		// 获取存储的数据并输出
		List<String> list = jedis.lrange("tutorial-list", 0, 5);
		for (int i = 0; i < list.size(); i++) {
			System.out.println("Stored list in redis:: " + list.get(i));
		}

		// 存储set类型数据
		jedis.sadd("turorial-set", "Redis");
		jedis.sadd("turorial-set", "Mongodb");
		jedis.sadd("turorial-set", "oracle");

		Set<String> set = jedis.smembers("turorial-set");
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			System.out.println("Stored set in redis:: " + key);
		}

		// 保存map对象
		Map<String,String> map = new HashMap<String,String>();
		map.put("bj", "北京");
		map.put("tj", "天津");
		jedis.hmset("redisMap", map);
		
		//  设置key有效期  时间 秒
		jedis.expire("sorted-set", 20);
		
		//  获取key 的有效期
		jedis.ttl("sorted-set");
		
		//  有序集合
		jedis.zadd("sorted-set", 1, "1");
		jedis.zadd("sorted-set", 2, "2");
		jedis.zadd("sorted-set", 3, "3");
		jedis.zadd("sorted-set", 4, "4");
		jedis.zadd("sorted-set", 5, "5");
		jedis.zadd("sorted-set", 6, "6");
		
		Set<String> sorted_set = jedis.zrange("sorted-set", 0, 10);
		it = sorted_set.iterator();
		while (it.hasNext()) {
			System.out.println(it.next()); 
		}
		
		// 获取key并输出
		Set<String> keylist = jedis.keys("*");
		for (String key : keylist) {
			System.out.println("redis set key is " + key);
		}
		
		jedis.close();
	}
}
