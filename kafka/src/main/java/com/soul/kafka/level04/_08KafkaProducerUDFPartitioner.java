package com.soul.kafka.level04;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class _08KafkaProducerUDFPartitioner {
    public static void main(String[] args) {
        //1.创建链接参数
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //指定分区策略, 默认为轮询, 可自定义
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, UserDefinePartitioner.class.getName());

        //2.创建生产者
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        //3.封账消息队列
        for (int i = 0; i < 10; i++) {
            //注意需要已存在的Topic
            //无key
            //ProducerRecord<String, String> record = new ProducerRecord<>("topic01", "value" + i);
            //有key
            ProducerRecord<String, String> record = new ProducerRecord<>("topic02", "K" + i, "V" + i);
            producer.send(record);
        }

        producer.close();
    }
}
