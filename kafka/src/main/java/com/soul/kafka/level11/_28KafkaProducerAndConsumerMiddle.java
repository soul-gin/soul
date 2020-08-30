package com.soul.kafka.level11;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;

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
public class _28KafkaProducerAndConsumerMiddle {
    // 消费者&生产者, 场景:
    // 当前消费端订阅上游(topic02)的消息(record),
    // 消费成功且成功将加工后的消息发送给下游(topic03)后,
    // 标记为上游消息成功消费(提交offset)

    //测试:
    // 创建 topic03:
    // kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --create --partitions 2 --replication-factor 2 --topic topic03
    // 查看创建的topic列表
    // kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --list
    // 运行 _27KafkaConsumerAfter 下游消费者服务器
    // 运行 _28KafkaProducerAndConsumerMiddle 消费者&生产者事务服务器
    // 运行 _29KafkaProducerBefore 生产发送上游消息
    // 发现 消息从middle(topic02)加工处理后, 流转到了下游after(topic03)
    public static void main(String[] args) {

        //构建生产者
        KafkaProducer<String, String> producer = buildKafkaProducer();
        //构建消费者
        KafkaConsumer<String, String> consumer = buildKafkaConsumer("group01");

        //初始化事务
        producer.initTransactions();

        //消费者订阅上游生产的数据 topic2
        consumer.subscribe(Arrays.asList("topic02"));

        try {
            while (true) {
                //consumer拉取数据
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                if (!consumerRecords.isEmpty()) {
                    //获取record数据迭代器
                    Iterator<ConsumerRecord<String, String>> consumerRecordIterator = consumerRecords.iterator();
                    //开启事务控制
                    producer.beginTransaction();
                    try {
                        //记录record元数据, 及消费端消费的消息的offset
                        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                        while (consumerRecordIterator.hasNext()) {
                            ConsumerRecord<String, String> record = consumerRecordIterator.next();
                            //记录元数据
                            offsets.put(new TopicPartition(record.topic(), record.partition()),
                                    //返回下次需要消费的消息的offset, 注意+1  !!!, 返回当前消息offset会重复消费当前消息
                                    new OffsetAndMetadata(record.offset() + 1));
                            System.out.println("middle flow key:" + record.key() + ", val:" + record.value()
                                    + ", partition:" + record.partition() + ", offset:" + record.offset());
                            //加工消费的上游消息, 创建新的Record交给下游处理
                            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("topic03",
                                    //加工消息value
                                    record.key(), record.value() + "_after_gin_soul_deal");
                            producer.send(producerRecord);

                        }
                        //提交消费端偏移量
                        producer.sendOffsetsToTransaction(offsets, "group01");
                        //提交事务
                        producer.commitTransaction();
                    } catch (ProducerFencedException e) {
                        System.out.println("error: " + e.getMessage());
                        //终止事务
                        producer.abortTransaction();
                    }
                }
            }
        } catch (Exception e) {
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

    public static KafkaConsumer<String, String> buildKafkaConsumer(String group) {
        //构建消费者配置信息
        Properties props = new Properties();
        //集群hostname:port
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //序列化 反序列化
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        //设置事务隔离级别, 读已提交(仅仅消费有提交标记的消息)
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // 消费者&生产者事务, 必须关闭消费端的自动提交
        // 应避免消费失败同时更新了offset, 导致无法重新消费"消费端失败"的消息
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new KafkaConsumer<>(props);
    }
}
