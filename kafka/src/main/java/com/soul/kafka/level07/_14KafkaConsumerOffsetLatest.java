package com.soul.kafka.level07;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
Kafka消费者默认对于未订阅的topic的offset的时候，
也就是系统并没有存储该消费者的消费分区的记录信息，
默认Kafka消费者的默认首次消费策略：latest

 auto.offset.reset=latest
earliest - 自动将偏移量重置为最早的偏移量
latest - 自动将偏移量重置为最新的偏移量
none - 如果未找到消费者组的先前偏移量，则向消费者抛出异常


 */
public class _14KafkaConsumerOffsetLatest {
    // offset 的 latest 策略
    // 1. kafka未记录过新接入的consumer的偏移量(offset)
    // consumer不会记录历史偏移量(offset), 也没有提交过历史偏移量
    // kafka会分配consumer启动时, 获取到的数据文件最新的偏移量(offset), 将该offset交给consumer
    // consumer消费后, 提交消费完的offset给kafka, 以便后续消费
    // 后续consumer宕机重启, 会从kafka拉取上次消费的offset进行消费, 这时不涉及 latest 策略
    // 故: latest 策略仅仅作用于新的consumer接入时

    //测试
    //1. 先启动 _13KafkaProducerOffset 发送5条消息
    //2. 再启动 _14KafkaConsumerOffsetLatest 发现未消费之前发送的消息
    //3. 再启动 _13KafkaProducerOffset 发送5条消息
    //4. 发现 _14KafkaConsumerOffsetLatest 消费了 _13KafkaProducerOffset 刚发送的5条消息
    public static void main(String[] args) {
        //1.创建Kafka链接参数
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //新的分组进行消费 group02
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group02");

        // 默认配置是latest
        // producer在 "新的consumer" 启动之前已经发送至broker的消息不会被处理
        // kafka默认以consumer启动时间来记录第一次的offset(老实人接盘, 我只在乎现在的你和以后的你)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        //2.创建Topic消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //3.订阅topic开头的消息队列
        consumer.subscribe(Pattern.compile("^topic.*$"));

        while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            Iterator<ConsumerRecord<String, String>> recordIterator = consumerRecords.iterator();
            while (recordIterator.hasNext()) {
                ConsumerRecord<String, String> record = recordIterator.next();
                String key = record.key();
                String value = record.value();
                long offset = record.offset();
                int partition = record.partition();
                System.out.println("key:" + key + ", value:" + value
                        + ", partition:" + partition + ", offset:" + offset);

            }
        }
    }
}
