package com.soul.kafka.level08;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 *
 Kafka生产者在发送完一个的消息之后，要求Broker在规定的额时间Ack应答答，
 如果没有在规定时间内应答，Kafka生产者会尝试n次重新发送消息。

 acks=1 默认
 acks=1 - Leader会将Record写到其本地日志中，但会在不等待所有Follower的完全确认的情况下做出响应。
 在这种情况下，如果Leader在确认记录后立即失败，但在Follower复制记录之前失败，则记录将丢失。
 acks=0 - 生产者根本不会等待服务器的任何确认。该记录将立即添加到套接字缓冲区中并视为已发送。
 在这种情况下，不能保证服务器已收到记录。
 acks=all - 这意味着Leader将等待全套同步副本确认记录。
 这保证了只要至少一个同步副本仍处于活动状态，记录就不会丢失。这是最有力的保证。这等效于acks = -1设置。

 如果生产者在规定的时间内，并没有得到Kafka的Leader的Ack应答，Kafka可以开启reties机制。
 request.timeout.ms = 30000  默认
 retries = 2147483647 默认
 *
 */
public class _20KafkaProducerAck {
    //测试
    //启动 _19KafkaConsumerAck 消费端服务器
    //启动 _20KafkaProducerAck 生产端服务器, 发送1条消息
    //发现 _19KafkaConsumerAck 消费了4次(正常1次+重试3次)
    public static void main(String[] args) {
        //创建链接参数
        Properties props = new Properties();
        //hostname:port
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //序列化反序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // -1 等价于 all, Leader将等待全套同步副本确认记录
        //props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ACKS_CONFIG, "-1");
        //重试3次
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        //设置1ms未收到ack则重试, 便于出现消费端重复消费问题
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1);

        //创建生产者
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        //消息队列发送消息, 仅仅发送一条
        for (int i = 0; i < 1; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("topic02",
                    "K" + i, "V" + i);
            producer.send(record);
        }

        producer.close();
    }
}
