package com.soul.base.anno.disruptor;

import com.lmax.disruptor.RingBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 生产者
 *
 * @author gin
 * @date 2021/4/16
 */
@Component
public class BehaviorEventProducer {

    @Autowired
    BehaviorEventBean behaviorEventBean;

    public void onData(Map<String, Object> behavior) {
        RingBuffer<BehaviorEvent> ringBuffer = behaviorEventBean.getDisruptor().getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            BehaviorEvent event = ringBuffer.get(sequence);
            event.setValue(behavior);
        } finally {
            ringBuffer.publish(sequence);
        }
    }


}
