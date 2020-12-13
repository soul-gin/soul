package com.soul.base.juc.level05;

import java.lang.ref.WeakReference;

/**
 * 弱引用遭到gc就会回收
 * ThreadLocal, WeakHashMap
 * 作用: 一般用在容器中
 * 强引用指向容器, 容器里的元素使用弱引用
 *
 */
public class T06_WeakReference {
    public static void main(String[] args) {
        WeakReference<T03_M> m = new WeakReference<>(new T03_M());

        System.out.println(m.get());
        System.gc();
        System.out.println(m.get());


        //这里会存在两个强引用
        //1. tl -> ThreadLocal
        //2. threadLocals -> key
        //一个弱引用:
        //3. key -> ThreadLocal
        //为什么需要使用弱引用:
        //如果是强引用, 当 tl=null, 但是key强引用ThreadLocal, 那么ThreadLocal在线程被回收后却无法被回收
        //弱引用可以避免这个问题, 但是还是会有内存泄漏风险, 当key的值变成null时, 导致整个value无法被访问到(但是value指向的对象无法被回收)
        //所以使用ThreadLocal时, 对应的value不再使用必须调用 remove 方法清理
        ThreadLocal<T03_M> tl = new ThreadLocal<>();
        // new T03_M()
        tl.set(new T03_M());
        //避免内存泄漏(无法被回收的空间)
        tl.remove();

    }
}

