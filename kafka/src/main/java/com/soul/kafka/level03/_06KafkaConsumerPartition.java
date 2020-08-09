package com.soul.kafka.level03;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class _06KafkaConsumerPartition {
    //先启动消费者, 再启动生产者
    public static void main(String[] args) {
        //1.创建Kafka链接参数
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //配置key的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //配置value的反序列化方式
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        //2.创建Topic消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //指定分区消费, 不配置消费组
        //props.put(ConsumerConfig.GROUP_ID_CONFIG, "group01");
        List<TopicPartition> partitions = Arrays.asList(new TopicPartition("topic02", 0));
        consumer.assign(partitions);
        //指定消费分区的位置, 从头开始seek消费
        consumer.seekToBeginning(partitions);
        //可以发现只会消费指定分区的数据
        //kafkaKey:kafkaKey1, kafkaValue:kafkaValue1,partition:0,offset:1

        //kafka仅仅保证单个procedure发送有序, 不保证消费有序
        while (true) {
            //每隔 1秒 拉取一条消息
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            Iterator<ConsumerRecord<String, String>> recordIterator = consumerRecords.iterator();
            while (recordIterator.hasNext()) {
                ConsumerRecord<String, String> record = recordIterator.next();
                String key = record.key();
                String value = record.value();
                long offset = record.offset();
                int partition = record.partition();
                System.out.println("kfkKey:" + key + ", kfkVal:" + value
                        + ", partition:" + partition + ", offset:" + offset);
            }
        }
    }
}
