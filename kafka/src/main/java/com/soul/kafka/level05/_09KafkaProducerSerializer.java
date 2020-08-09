package com.soul.kafka.level05;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Date;
import java.util.Properties;

public class _09KafkaProducerSerializer {
    public static void main(String[] args) {
        //1.创建链接参数
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //使用自定义序列化类
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectSerializer.class.getName());

        //2.创建生产者
        KafkaProducer<String, User> producer = new KafkaProducer<>(props);

        //3.封账消息队列
        for (int i = 0; i < 10; i++) {
            //注意需要已存在的Topic
            ProducerRecord<String, User> record = new ProducerRecord<>("topic02", "K" + i,
                    new User(i, "soul-gin" + i, new Date()));
            producer.send(record);
        }

        producer.close();
    }
}
