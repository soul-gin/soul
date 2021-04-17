package com.soul.jmh.disruptor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.nio.ByteBuffer;

public class Main01
{
    public static void main(String[] args) throws Exception
    {
        // The factory for the event
        // 初始化事件工厂
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        // 一般2的n次方, 划分位置个数
        int bufferSize = 1024;

        // Construct the Disruptor
        // 指定消息工厂, 环切分的个数(类似槽位), 线程工厂(消费者线程)
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, Executors.defaultThreadFactory());

        // Connect the handler
        // 设定处理消息的处理器(handler)
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        //生产端测试代码
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        //官方例程
        //获取下一个可用的位置
        // Grab the next sequence
        long sequence = ringBuffer.next();
        try
        {
            LongEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence, Fill with data
            //设置消息
            event.set(8888L);
        }
        finally
        {
            //消息发布
            ringBuffer.publish(sequence);
        }

    }
}