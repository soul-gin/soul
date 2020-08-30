package com.soul.kafka.level11;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

/**
 *
Kafka的幂等性，只能保证一条记录的在分区发送的原子性，
但是如果要保证多条记录（多分区）之间的完整性，这个时候就需要开启kafk的事务操作。

在Kafka0.11.0.0除了引入的幂等性的概念，同时也引入了事务的概念。
通常Kafka的事务分为 生产者事务Only、消费者&生产者事务。
一般来说默认消费者消费的消息的级别是read_uncommited数据，这有可能读取到事务失败的数据，
所有在开启生产者事务之后，需要用户设置消费者的事务隔离级别。

isolation.level	=  read_uncommitted 默认
该选项有两个值read_committed|read_uncommitted，如果开始事务控制，
消费端必须将事务的隔离级别设置为read_committed

开启的生产者事务的时候，只需要指定transactional.id属性即可，
一旦开启了事务，默认生产者就已经开启了幂等性。
但是要求"transactional.id"的取值必须是唯一的，
同一时刻只能有一个"transactional.id"存储在，其他的将会被关闭。

 *
 */
public class _29KafkaProducerBefore {
    public static void main(String[] args) {

        //构建生产者
        KafkaProducer<String, String> producer = buildKafkaProducer();

        //初始化事务
        producer.initTransactions();

        try {
            //开启事务控制
            producer.beginTransaction();
            for (int i = 0; i < 10; i++) {
                //创建Record
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>("topic02",
                        "K" + i, "V" + i);
                producer.send(producerRecord);
                //为了测试使用, 每条都刷, 避免没有数据写入到kafka
                producer.flush();
            }
            //避免本地缓存数据, 将消息刷到kafka集群
            producer.flush();
            //提交事务
            producer.commitTransaction();
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            //终止事务
            producer.abortTransaction();
        } finally {
            producer.close();
        }
    }

    public static KafkaProducer<String, String> buildKafkaProducer() {
        //构建生产者配置信息
        Properties props = new Properties();
        //集群hostname:port
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //序列化 反序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //配置生产者端事务ID
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,
                //避免重复, 拼接 UUID 来保证唯一
                "transaction-id" + ":" + UUID.randomUUID().toString());
        //配置批处理数量, 默认为16384字节, 测试时设置较小(多条数据缓冲到设定字节后, 打包一次发送)
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024);
        //为避免长时间未达到发送批次上线, 导致数据不发送, 设置必须发送的时间间隔, 毫秒, 5ms
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        //配置重试机制(all-至少一个副本写入数据, 20S未接收到应答则重复发送)
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 20000);
        //开启幂等机制
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new KafkaProducer<>(props);
    }

}
