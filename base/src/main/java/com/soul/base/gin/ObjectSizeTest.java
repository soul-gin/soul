package com.soul.base.gin;

import org.apache.lucene.util.RamUsageEstimator;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * @author gin
 * @date 2021/3/9
 */
public class ObjectSizeTest {

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

    public static void main(String[] args) {

        //unsafeSizeTest();

        luceneSizeTest();

    }

    private static void luceneSizeTest() {
        byte[] bytes = new byte[1024 * 1024];
        System.out.println("--------bytes--------");
        System.out.println(RamUsageEstimator.sizeOf(bytes));
        System.out.println(RamUsageEstimator.shallowSizeOf(bytes));
        System.out.println(RamUsageEstimator.humanSizeOf(bytes));
        System.out.println("--------bytes--------");
        System.out.println();

        SizeTest sizeTest = new SizeTest();


        System.out.println("--------hashMap--------");
        //计算指定对象及其引用树上的所有对象的综合大小，单位字节
        System.out.println(RamUsageEstimator.sizeOf(sizeTest.getHashMap()));

        //计算指定对象本身在堆空间的大小，单位字节
        System.out.println(RamUsageEstimator.shallowSizeOf(sizeTest.getHashMap()));

        //计算指定对象及其引用树上的所有对象的综合大小，返回可读的结果，如：2KB
        System.out.println(RamUsageEstimator.humanSizeOf(sizeTest.getHashMap()));
        System.out.println("--------hashMap--------");
        System.out.println();

        System.out.println("--------bitMap--------");
        //计算指定对象及其引用树上的所有对象的综合大小，单位字节
        System.out.println(RamUsageEstimator.sizeOf(sizeTest.getRoaring64NavigableMap()));

        //计算指定对象本身在堆空间的大小，单位字节
        System.out.println(RamUsageEstimator.shallowSizeOf(sizeTest.getRoaring64NavigableMap()));

        //计算指定对象及其引用树上的所有对象的综合大小，返回可读的结果，如：2KB
        System.out.println(RamUsageEstimator.humanSizeOf(sizeTest.getRoaring64NavigableMap()));
        System.out.println("--------bitMap--------");
    }

    private static void unsafeSizeTest() {
        Field[] fields = SizeTest.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName() + "---offSet:" + THE_UNSAFE.objectFieldOffset(field));
        }
    }



}
