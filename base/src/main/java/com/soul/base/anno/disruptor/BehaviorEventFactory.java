package com.soul.base.anno.disruptor;

import com.lmax.disruptor.EventFactory;


/**
* @author gin
* @date 2021/4/16
*/
public class BehaviorEventFactory implements EventFactory<BehaviorEvent> {
    @Override
    public BehaviorEvent newInstance() {
        return new BehaviorEvent();
    }
}