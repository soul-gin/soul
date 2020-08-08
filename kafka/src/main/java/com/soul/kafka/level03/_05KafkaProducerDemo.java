package com.soul.kafka.level03;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class _05KafkaProducerDemo {
    //先启动消费者, 再启动生产者
    public static void main(String[] args) throws InterruptedException {
        //1.创建链接参数
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //配置key的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //配置value的序列化方式
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //2.创建生产者
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        //3.封账消息队列
        for (Integer i = 0; i < 10; i++) {
            Thread.sleep(100);
            //生产消息
            ProducerRecord<String, String> record = new ProducerRecord<>("topic02", "kafkaKey" + i, "kafkaValue" + i);
            //发送消息
            producer.send(record);
        }

        //关闭生产者
        producer.close();
    }
}
