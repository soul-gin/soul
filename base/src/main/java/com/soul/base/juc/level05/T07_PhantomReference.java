package com.soul.base.juc.level05;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.List;

/**
 *     一个对象是否有虚引用的存在，完全不会对其生存时间构成影响，
 *     也无法通过虚引用来获取一个对象的实例。
 *     为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知。
 *     虚引用和弱引用对关联对象的回收都不会产生影响，如果只有虚引用活着弱引用关联着对象，
 *     那么这个对象就会被回收。它们的不同之处在于弱引用的get方法，虚引用的get方法始终返回null,
 *     弱引用可以使用ReferenceQueue,虚引用必须配合ReferenceQueue使用。
 *
 *     jdk中直接内存的回收就用到虚引用，由于jvm自动内存管理的范围是堆内存，
 *     而直接内存是在堆内存之外（其实是内存映射文件，自行去理解虚拟内存空间的相关概念），
 *     所以直接内存的分配和回收都是有Unsafe类去操作，java在申请一块直接内存之后，
 *     会在堆内存分配一个对象保存这个堆外内存的引用，
 *     这个对象被垃圾收集器管理，一旦这个对象被回收，
 *     相应的用户线程会收到通知并对直接内存进行清理工作。
 *
 *     事实上，虚引用有一个很重要的用途就是用来做堆外内存的释放，
 *     DirectByteBuffer就是通过虚引用来实现堆外内存的释放的。
 *
 */
public class T07_PhantomReference {
    private static final List<Object> LIST = new LinkedList<>();
    private static final ReferenceQueue<T03_M> QUEUE = new ReferenceQueue<>();



    public static void main(String[] args) {

        //只要有垃圾回收就会回收掉虚引用, 主要用于管理堆外内存
        //创建虚引用必须有: 创建的引用对象, 队列
        PhantomReference<T03_M> phantomReference = new PhantomReference<>(new T03_M(), QUEUE);

        //注意: 测试时需要修改 VM options
        // -Xms20M -Xmx20M

        //一直申请堆内存
        new Thread(() -> {
            while (true) {
                LIST.add(new byte[1024 * 1024]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                //get拿不到虚引用的数据, 主要作用是处理 DirectByteBuffer (直接内存)
                //直接内存属于堆外内存空间, 不直接被虚拟机(jvm)管理
                //需要通过虚引用监控其状态, 在对象被jvm垃圾回收时, 需要同时清理堆外内存(通过队列通知)
                System.out.println(phantomReference.get());
            }
        }).start();

        //监测队列, 查看是否有被回收掉的虚引用(一旦被回收就会把回收的信息存在队列中, 可以在队列中监控回收情况)
        new Thread(() -> {
            while (true) {
                Reference<? extends T03_M> poll = QUEUE.poll();
                if (poll != null) {
                    System.out.println("PhantomReference finalized by jvm: " + poll);
                }
            }
        }).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

