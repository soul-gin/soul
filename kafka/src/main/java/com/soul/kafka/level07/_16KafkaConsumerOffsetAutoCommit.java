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
 *
Kafka消费者在消费数据的时候默认会定期的提交消费的偏移量，
这样就可以保证所有的消息至少可以被消费者消费1次,
用户可以通过以下两个参数配置：

enable.auto.commit = true  默认
auto.commit.interval.ms = 5000 默认

如果需要自己管理offset的自动提交，
可以关闭offset的自动提交，手动管理offset提交的偏移量，
注意提交的offset偏移量永远都要比本次消费的偏移量+1，
因为提交的offset是kafka消费者下一次抓取数据的位置。
 *
 */
public class _16KafkaConsumerOffsetAutoCommit {
    //测试
    //1. 先启动 _13KafkaProducerOffset 发送5条消息
    //2. 再启动 _16KafkaConsumerOffsetAutoCommit 消费了 _13KafkaProducerOffset 刚发送的5条消息
    //3. 再关闭 _16KafkaConsumerOffsetAutoCommit 需10s内处理
    //4. 再启动 _16KafkaConsumerOffsetAutoCommit 又消费了 _13KafkaProducerOffset 刚发送的5条消息, 等10s, 关闭
    //5. 再启动 _16KafkaConsumerOffsetAutoCommit 未消费
    //6. 再启动 _13KafkaProducerOffset 发送5条消息
    //7. 发现 _16KafkaConsumerOffsetAutoCommit 开始消费新数据, 即offset提交成功至kafka才会开始新的消费
    public static void main(String[] args) {
        //1.创建Kafka链接参数
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //新的分组进行消费 group04
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group04");

        // 默认配置是latest
        // earliest 从分区最早未消费的offset开始消费(PUA男"接盘", 以前的你, 现在的你和以后的你, 都是我喜欢的你)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //将offset自动提交时间间隔延长 10s
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 10000);
        //默认值 true, 开启自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

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
