package com.soul.springbootkafka;

import com.soul.springbootkafka.level15.MyMessageSender;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootKafkaApplicationTests {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testSendMessage() {
        //非事务发送消息
        //注意: 优先修改 application.properties
        //不能配置 spring.kafka.producer.transaction-id-prefix
        //或
        //注释掉配置 spring.kafka.producer.transaction-id-prefix=transaction-id-
        kafkaTemplate.send(new ProducerRecord<>("topic02", "001", "nier_28_001_kafka_message"));
    }

    @Test
    public void testSendMessageTransaction() {
        //注意: 优先修改 application.properties
        //配置 spring.kafka.producer.transaction-id-prefix=transaction-id-
        //事务发送消息
        kafkaTemplate.executeInTransaction(new KafkaOperations.OperationsCallback<String, String, Object>() {
            @Override
            public Object doInOperations(KafkaOperations<String, String> kafkaOperations) {
                kafkaOperations.send(new ProducerRecord<>("topic02", "002", "nier_28_002_kafka_message"));
                return null;
            }
        });

    }

    @Autowired
    private MyMessageSender myMessageSender;

    @Test
    public void testSendMessage3() {
        //spring事务发送消息
        myMessageSender.sendMessage("topic02", "003", "nier_28_003_kafka_message");
    }

}
