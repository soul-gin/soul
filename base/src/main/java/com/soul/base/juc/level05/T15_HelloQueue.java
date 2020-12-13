package com.soul.base.juc.level05;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Queue主要是针对多线程高并发提供了很多方法
 * 阻塞 插入/获取
 */
public class T15_HelloQueue {
    public static void main(String[] args) {
        Queue<Integer> q = new ArrayBlockingQueue<>(2);
        q.add(0);
        q.add(1);
        q.add(2);
        q.add(3);
        System.out.println(q);

    }
}
