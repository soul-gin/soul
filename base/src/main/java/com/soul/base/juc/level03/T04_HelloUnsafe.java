package com.soul.base.juc.level03;

import sun.misc.Unsafe;

/**
 * Unsafe操作
 * 直接操作内存
 * allocateMemory putXXX  分配内存
 * freeMemory pageSize 释放内存
 * 直接生成实例
 * allocateInstance
 * 直接操作类或者实例变量
 * objectFieldOffset
 * getInt
 * getObject
 * Cas相关操作
 * weakCompareAndSetObject
 */
public class T04_HelloUnsafe {
    static class M {
        private M() {}

        int i =0;
    }

   public static void main(String[] args) throws InstantiationException {
        //注意jdk8直接使用会在get方法中抛出异常, 需要通过反射使用
        //jdk11开始可以直接使用
        Unsafe unsafe = Unsafe.getUnsafe();
        M m = (M)unsafe.allocateInstance(M.class);
        m.i = 9;
        System.out.println(m.i);
    }
}


