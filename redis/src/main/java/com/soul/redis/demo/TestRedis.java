package com.soul.redis.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author gin
 */
@Component
public class TestRedis {

    //redis 默认序列化的 Template
    @Autowired
    private RedisTemplate redisTemplate;

    //spring封装的 Template
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("hashTemplate")
    private StringRedisTemplate hashTemplate;

    public void testRedis(){
        //设置
        redisTemplate.opsForValue().set("k1", 1);
        //获取设置的值
        System.out.println("k1=" + redisTemplate.opsForValue().get("k1"));

        //使用spring封装的高级api
        stringRedisTemplate.opsForValue().set("k2", "gin");
        //获取设置的值
        System.out.println("k2=" + stringRedisTemplate.opsForValue().get("k2"));


        //低级api
        RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
        conn.set("k3".getBytes(), "soul".getBytes());
        //获取设置的值
        System.out.println("k3=" + new String(conn.get("k3".getBytes())));


        //hash
        HashOperations<String, Object, Object> hash = stringRedisTemplate.opsForHash();
        hash.put("k4", "name", "银魂");
        hash.put("k4", "age", "28");
        //获取设置的值
        System.out.println("k4=" + hash.entries("k4"));


        //测试对象存储1
        Person p = new Person();
        p.setName("naruto");
        p.setAge(16);

        Jackson2HashMapper jm = new Jackson2HashMapper(objectMapper, false);
        redisTemplate.opsForHash().putAll("k5", jm.toHash(p));
        //获取设置的值
        Map k5 = redisTemplate.opsForHash().entries("k5");
        System.out.println("k5=" + objectMapper.convertValue(k5, Person.class));

        //测试对象存储2
        //为避免序列化Integer类型报错, 使用json序列化
        stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        stringRedisTemplate.opsForHash().putAll("k6", jm.toHash(p));
        //获取设置的值
        Map k6 = stringRedisTemplate.opsForHash().entries("k6");
        System.out.println("k6=" + objectMapper.convertValue(k6, Person.class));

        //测试对象存储3
        //为避免序列化Integer类型报错, 使用json序列化
        hashTemplate.opsForHash().putAll("k7", jm.toHash(p));
        //获取设置的值
        Map k7 = hashTemplate.opsForHash().entries("k7");
        System.out.println("k7=" + objectMapper.convertValue(k6, Person.class));


        //发布订阅
        //订阅通道
        //也可以通过redis-cli中执行命令: subscribe myChannel
        conn.subscribe(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] bytes) {
                byte[] body = message.getBody();
                System.out.println("message=" + new String(body));
            }
        }, "myChannel".getBytes());

        //发布消息
        //也可以通过redis-cli中执行命令: publish myChannel hello gin
        stringRedisTemplate.convertAndSend("myChannel", "hello world");

        while (true){

        }

    }

}
