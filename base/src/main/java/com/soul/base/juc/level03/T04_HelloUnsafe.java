package com.soul.base.juc.level03;

import com.soul.base.gin.ObjectSizeTest;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

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
        private M() {
        }

        int i = 0;
    }

    private static final Unsafe THE_UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                @Override
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };
            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    public static void main(String[] args) throws InstantiationException {
        //注意jdk8直接使用会在get方法中抛出异常, 需要通过反射使用
        //Unsafe unsafe = Unsafe.getUnsafe();
        //jdk11开始可以直接使用
        M m = (M) THE_UNSAFE.allocateInstance(M.class);
        m.i = 9;
        Field[] fields = M.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName() + "---offSet:" + THE_UNSAFE.objectFieldOffset(field));
        }
        System.out.println(m.i);
    }
}


