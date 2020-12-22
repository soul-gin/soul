package com.soul.jmh.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * 定义Event工厂，用于填充队列
 *
 *  这里牵扯到效率问题：disruptor初始化的时候，会调用Event工厂，对ringBuffer进行内存的提前分配
 *  GC产频率会降低
 */
public class LongEventFactory implements EventFactory<LongEvent> {

    @Override
    public LongEvent newInstance() {
        //可以new或者从数据库里查询, 对外只暴露 newInstance 获取event
        return new LongEvent();
    }
}
