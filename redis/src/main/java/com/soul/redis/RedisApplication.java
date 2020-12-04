package com.soul.redis;

import com.soul.redis.demo.TestRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RedisApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RedisApplication.class, args);
        //获取上下文对象, 以便获取被spring托管的对象
        TestRedis redis = ctx.getBean(TestRedis.class);
        //启动成功后直接调起对应测试方法
        redis.testRedis();
    }

}
