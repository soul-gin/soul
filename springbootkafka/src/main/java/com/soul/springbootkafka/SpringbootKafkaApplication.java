package com.soul.springbootkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.messaging.handler.annotation.SendTo;

import java.io.IOException;

@SpringBootApplication
public class SpringbootKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootKafkaApplication.class, args);
        //挂起程序
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @KafkaListeners(
            value = {
                    //监听 topic01
                    @KafkaListener(topics = {"topic01"})
            }
    )
    public void received(ConsumerRecord<String, String> record){
        System.out.println("record:" + record);
    }

    @KafkaListeners(
            value = {
                    //监听 topic01
                    @KafkaListener(topics = {"topic02"})
            }
    )
    //发送消息
    @SendTo("topic03")
    public String received2(ConsumerRecord<String, String> record){
        //将topic02的消息加工后交由topic03进行处理
        //可以在kafka集群的 bin 目录下执行:
        // ./kafka-console-consumer.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --topic topic03
        // 监听消费 topic03 或者, 由 kafka-eagle 的 http://192.168.25.106:8048/ke/topic/message 查询收到消息
        // 再由 kafka-eagle 的mock模式向 topic02 发送消息
        // http://192.168.25.106:8048/ke/topic/mock
        return record.value() + " after gin deal";
    }

}
