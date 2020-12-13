package com.soul.base.juc.level05;

public class T03_M {
    @Override
    protected void finalize() throws Throwable {
        //垃圾回收会调用 finalize 方法
        //打印可以看到jvm已经开始回收当前对象了
        System.out.println("finalize");
    }
}
