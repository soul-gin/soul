package com.soul.kafka.level09;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 *
 HTTP/1.1中对幂等性的定义是：一次和多次请求某一个资源对于资源本身应该具有同样的结果（网络超时等问题除外）。
 也就是说，其任意多次执行对资源本身所产生的影响均与一次执行的影响相同。

 Methods can also have the property of “idempotence” in that (aside from error or expiration issues)
 the side-effects of N > 0 identical requests is the same as for a single request.

 Kafka在0.11.0.0版本支持增加了对幂等的支持。幂等是针对生产者角度的特性。
 幂等可以保证上生产者发送的消息，不会丢失，而且不会重复。实现幂等的关键点就是服务端可以区分请求是否重复，
 过滤掉重复的请求。要区分请求是否重复的有两点：
 唯一标识(PID)：要想区分请求是否重复，请求中就得有唯一标识。例如支付请求中，订单号就是唯一标识
 记录下已处理过的请求标识：光有唯一标识还不够，还需要记录下那些请求是已经处理过的，这样当收到新的请求时，
 用新请求中的标识(Seq)和处理记录标识(Seq)进行比较，如果处理记录中有相同的标识，说明是重复记录，拒绝掉。


 场景: 一笔交易, 交易号唯一, 涉及需要发送多条消息(组合唯一索引: PID, Seq)
 请求1: PID=ooxx, Seq=1 PID=ooxx不存在broker缓存中, 请求1 写入磁盘, 将PID和Seq写入broker缓存中
 请求2: PID=ooxx, Seq=1, PID=ooxx存在broker缓存中, 取当前请求的Seq与缓存中的Seq(上次写盘数据)比对
 发现Seq相等(说明该消息为重试消息,PID,Seq沿用第一次发送的值), 请求2 被拒绝写入磁盘
 请求3: PID=ooxx, Seq=2, PID=ooxx存在broker缓存中, 取当前请求的Seq与缓存中的Seq(上次写盘数据)比对
 发现当前Seq大于缓存中的Seq, 请求3 写入磁盘, 更新Seq为2


 幂等又称为exactly once。(精准一次, 不丢消息, 且只消费一次)
 要停止多次处理消息，必须仅将其持久化到Kafka Topic中仅仅一次。
 在初始化期间，kafka会给生产者生成一个唯一的ID称为Producer ID或PID。

 PID和序列号(Seq)与消息捆绑在一起，然后发送给Broker。由于序列号(Seq)从零开始并且单调递增，
 因此，仅当消息的序列号比该PID / TopicPartition对中最后提交的消息正好大1时，Broker才会接受该消息。
 如果不是这种情况，则Broker认定是生产者重新发送该消息。

 enable.idempotence= false 默认(不开启幂等)

 注意:在使用幂等性的时候，要求必须开启retries=true和acks=all
 且 max.in.flight.requests.per.connection 必须小于等于 5, producer端有超过对应设置值没有被应答则会阻塞链接
 另值2-5, 会出现一条record发送, 重试, 阻塞; 后续消息正常发送, 则会出现消息发送乱序
 若想要保证发送消息严格有序, 则需要设置为 1 即可

 *
 */
public class _23KafkaProducerIdempotence {
    //测试
    //启动 _22KafkaConsumerIdempotence 消费端服务器
    //启动 _23KafkaProducerIdempotence 生产端服务器, 发送1条消息
    //发现 _22KafkaConsumerIdempotence 消费了1次, 没有重复消费, 幂等属性生效
    public static void main(String[] args) {
        //创建链接参数
        Properties props = new Properties();
        //hostname:port
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka01:9092,kafka02:9092,kafka03:9092");
        //序列化反序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // -1 等价于 all, Leader将等待全套同步副本确认记录
        // 幂等情况下, ack必须-1或all
        //props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ACKS_CONFIG, "-1");
        //重试3次(幂等情况下, retries必须大于0)
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        //设置1ms未收到ack则重试, 便于出现消费端重复消费问题
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1);
        //开启幂等操作(精准一次)
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        //保证消息发送严格有序则设置为1, 若不需要则可以设置 2-5 (不需要幂等, 可以设置大于5)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

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
