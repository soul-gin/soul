package com.soul.base.anno.disruptor;

import com.lmax.disruptor.EventHandler;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 消费者
 *
 * @author gin
 * @date 2021/4/16
 */
public class BehaviorEventHandler implements EventHandler<BehaviorEvent> {

    CopyOnWriteArrayList<BehaviorEvent> behaviorEventList = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(BehaviorEvent event, long sequence, boolean endOfBatch) {
        behaviorEventList.add(event);
        if (behaviorEventList.size() >= 3) {
            //synchronized (this) {
                LinkedList<BehaviorEvent> partEvent = new LinkedList<>(behaviorEventList);
                behaviorEventList.clear();
                if (partEvent.size() != 3){
                    System.err.println("error:" + partEvent.size());
                }
                //System.out.println(partEvent.size());
                /*for (BehaviorEvent behaviorEvent : partEvent) {
                    System.out.println("Event: " + behaviorEvent);
                }*/
            //}
        }

    }
}