package com.soul.jmh.disruptor;

/**
 * 定义Event - 队列中需要处理的元素
 */
public class LongEvent
{
    //消息的value, 可以装任何类型, 目前是装数字
    private long value;

    public void set(long value)
    {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LongEvent{" +
                "value=" + value +
                '}';
    }
}
