package com.soul.base.io.level05;


public class MainThread {

    public static void main(String[] args) {
        //这里不做关于IO 和 业务的事情

        //1.创建 IO Thread (一个或者多个)
        //boss又自己的线程组(一般一个线程足够, 看需要绑定几个端口)
        SelectorThreadGroup boss = new SelectorThreadGroup(2);
        //worker也有自己的线程组
        SelectorThreadGroup worker = new SelectorThreadGroup(3);
        //在boss中设置worker线程组, boss得持有worker的引用
        boss.setWorker(worker);

        //2.把监听的 server 注册到某一个selector上
        //未来 listen 一旦accept得到client后得去worker中next一个线程分配
        boss.bind(9999);
        boss.bind(8888);

        //测试(通过shell连接本机, vmware虚拟机, .1 是与本机的通讯ip)
        // yum install nc -y
        // yum install nmap -y
        // nc 192.168.25.1 9999
        // nc 192.168.25.1 8888
        // 发送数据, 原数据返回


    }


}
