package com.soul.base.juc.level05;

import java.lang.ref.SoftReference;

/**
 * 软引用
 * 软引用是用来描述一些还有用但并非必须的对象。
 * 对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围进行第二次回收。
 * 如果这次回收还没有足够的内存，才会抛出内存溢出异常。
 *
 * 注意: 测试需要修改 VM options
 * -Xms20M -Xmx20M
 */
public class T05_SoftReference {
    public static void main(String[] args) {
        //软引用只有内存不够使用时才会回收, 内存够不会回收
        //软引用非常适合缓存使用
        SoftReference<byte[]> m = new SoftReference<>(new byte[1024*1024*10]);
        //m = null;
        System.out.println(m.get());
        //full gc
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //内存还足够, 发现软引用不会被回收
        System.out.println(m.get());

        //再分配一个数组，heap将装不下，这时候系统会垃圾回收，先回收一次，如果不够，会把软引用干掉
        //测试时避免直接oom, b分配不要过大
        byte[] b = new byte[1024*1024*11];
        //会打印 null
        System.out.println(m.get());
    }
}

