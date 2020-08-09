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
public class _17KafkaConsumerOffsetUDFCommit {
    public static void main(String[] args) {
        //1.创建Kafka链接参数
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //新的分组进行消费 group05
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group05");
        //关闭自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        //2.创建Topic消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //3.订阅topic开头的消息队列
        consumer.subscribe(Pattern.compile("^topic.*$"));

        while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            //从队列中获取到了消息数据
            if (!consumerRecords.isEmpty()) {
                Iterator<ConsumerRecord<String, String>> recordIterator = consumerRecords.iterator();
                while (recordIterator.hasNext()) {
                    ConsumerRecord<String, String> record = recordIterator.next();
                    String key = record.key();
                    String value = record.value();
                    //提交的offset偏移量永远都要比本次消费的偏移量+1
                    //否则每次consumer重启会读取上次的最后一条消息, 并重复消费该消息
                    long offset = record.offset();
                    int partition = record.partition();
                    //offsets用于记录分区每个消息消费完成后的偏移量offset
                    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<TopicPartition, OffsetAndMetadata>();
                    //消费的元数据信息, 及offset
                    offsets.put(new TopicPartition(record.topic(), partition),
                            //提交的offset偏移量永远都要比本次消费的偏移量+1 !!!
                            //否则每次consumer重启会读取上次的最后一条消息, 并重复消费该消息
                            new OffsetAndMetadata(offset + 1));
                    //异步提交消费者offset
                    consumer.commitAsync(offsets, new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                            //提交回调信息
                            System.out.println("完成：" + offset + "提交！");
                        }
                    });
                    System.out.println("key:" + key + ", value:" + value
                            + ", partition:" + partition + ", offset:" + offset);

                }
            }
        }
    }
}
