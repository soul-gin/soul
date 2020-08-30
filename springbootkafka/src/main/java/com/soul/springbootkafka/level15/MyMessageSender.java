package com.soul.springbootkafka.level15;

public interface MyMessageSender {
    void sendMessage(String topic, String key, String message);
}
