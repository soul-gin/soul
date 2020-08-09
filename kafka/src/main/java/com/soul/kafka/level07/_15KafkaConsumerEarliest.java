package com.soul.kafka.level07;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

public class _15KafkaConsumerEarliest {
    // offset 的 earliest 策略
    // 1. kafka未记录过新接入的consumer的偏移量(offset)
    // consumer不会记录历史偏移量(offset), 也没有提交过历史偏移量
    // kafka会分配读取到分区的数据文件最早的偏移量(offset), 分配给consumer
    // consumer消费后, 提交消费完的offset给kafka, 以便后续消费
    // 后续consumer宕机重启, 会从kafka拉取上次消费的offset进行消费, 这时不涉及 latest 策略
    // 故: earliest 策略也是仅仅作用于新的consumer接入时

    //测试
    //1. 先启动 _13KafkaProducerOffset 发送5条消息
    //2. 再启动 _14KafkaConsumerOffsetLatest 发现消费了 _13KafkaProducerOffset 刚发送的5条消息
    public static void main(String[] args) {
        //1.创建Kafka链接参数
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //新的分组进行消费 group03
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group03");

        // 默认配置是latest
        // earliest 从分区最早未消费的offset开始消费(PUA男"接盘", 以前的你, 现在的你和以后的你, 都是我喜欢的你)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

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
