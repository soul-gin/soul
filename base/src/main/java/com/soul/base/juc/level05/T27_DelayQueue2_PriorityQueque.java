package com.soul.base.juc.level05;

import java.util.PriorityQueue;

public class T27_DelayQueue2_PriorityQueque {

    public static void main(String[] args) {

        //实现通过二叉树
        //优先级队列
        //DelayQueue 本质上是个PriorityQueue
        PriorityQueue<String> q = new PriorityQueue<>();

        //队列添加时, 内部进行了排序
        q.add("c");
        q.add("e");
        q.add("a");
        q.add("d");
        q.add("z");

        //按字典顺序获取
        for (int i = 0; i < 5; i++) {
            System.out.println(q.poll());
        }

    }
}
