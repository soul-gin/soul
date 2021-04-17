package com.soul.jmh.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.*;

/**
 * 生产者模式
 */
public class Main04_ProducerType
{
    public static void main(String[] args) throws Exception
    {
        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        // Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, Executors.defaultThreadFactory());

        //指定单线程模式
        // ProducerType.MULTI 多线程方式(默认)
        // ProducerType.SINGLE 单线程模式, 对sequence访问不会加锁, 必须保证线程只有一个, 否则会出现覆盖问题
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, Executors.defaultThreadFactory(),
                ProducerType.SINGLE, new BlockingWaitStrategy());

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        //================================================================================================
        final int threadCount = 50;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        ExecutorService service = Executors.newCachedThreadPool();
        for (long i = 0; i < threadCount; i++) {
            final long threadNum = i;
            service.submit(()-> {
                System.out.printf("Thread %s ready to start!\n", threadNum );
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

                //生产5000条消息
                for (int j = 0; j < 100; j++) {
                    ringBuffer.publishEvent((event, sequence) -> {
                        event.set(threadNum);
                        System.out.println("生产了 " + threadNum);
                    });
                }


            });
        }

        service.shutdown();
        //disruptor.shutdown();
        TimeUnit.SECONDS.sleep(3);
        //实际消费的数量, 发现多线程处理情况下必须使用 ProducerType.MULTI 模式, 使用 ProducerType.SINGLE 会出现不加锁的并发覆盖问题
        System.out.println(LongEventHandler.count);
    }

}