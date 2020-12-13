package com.soul.base.juc.level05;

import java.io.IOException;

/**
 * 引用类型
 * 强(new )
 * 软(大对象的缓存, 常用对象的缓存)
 * 弱(缓存, 没有容器引用指向的时候就需要清除的缓存; ThreadLocal, WeakHashMap)
 * 虚(管理堆外内存)
 *
 */
public class T04_NormalReference {
    public static void main(String[] args) throws IOException {
        T03_M t03M = new T03_M();
        //只有没有引用指向了(new 出来的对象), 才会回收该对象
        t03M = null;
        System.gc(); //DisableExplicitGC

        System.in.read();
    }
}
