package com.soul.kafka.level06;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class UserDefineProducerInterceptor implements ProducerInterceptor {
    @Override
    public ProducerRecord onSend(ProducerRecord record) {
        // 拦截发送的record, 在value后面追加信息
        ProducerRecord wrapRecord = new ProducerRecord(record.topic(), record.key(),
                record.value() + "--add something");
        // 截发送的record, 在头上增加String类型的key 和 字节数组类型的value
        // [RecordHeader(key = gin, value = [95, 115, 111, 117, 108, 95])]
        wrapRecord.headers().add("gin", "_soul_".getBytes());
        return wrapRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        //每当 成功/失败 发送一条record, 都会触发该回调方法
        //成功返回 metadata, 失败返回 exception
        System.out.println("metadata:" + metadata + ", exception:" + exception);
    }

    @Override
    public void close() {
        //生产者关闭后的回调, 如做一些资源的释放
        System.out.println("close");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        //可以从 configs 中获取生产者信息
        System.out.println("configure");
    }
}
