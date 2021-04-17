package com.soul.base.anno.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author gin
 * @date 2021/4/16
 */
@Component
public class BehaviorEventBean {

    /**
     * RingBuffer 大小，必须是 2 的 N 次方
     * 1 << 10
     */
    public static final int RING_BUFFER_SIZE = 2;

    Disruptor<BehaviorEvent> disruptor;

    @PostConstruct
    public void init() {
        System.out.println("begin...");
        // The factory for the event
        EventFactory<BehaviorEvent> eventFactory = new BehaviorEventFactory();

        disruptor = new Disruptor<>(eventFactory,
                RING_BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE,
                new YieldingWaitStrategy());

        EventHandler<BehaviorEvent> eventHandler = new BehaviorEventHandler();
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();
        System.out.println("init " + (disruptor != null) + " end...");
    }

    public Disruptor<BehaviorEvent> getDisruptor() {
        return disruptor;
    }
}