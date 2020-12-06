package com.soul.base.io.level04;


public class MainThread {

    public static void main(String[] args) {
        //这里不做关于IO 和 业务的事情

        //1.创建 IO Thread (一个或者多个)
        //混杂模式, 只有一个线程负责accept, 其他每个都会被分配client, 进行R/W
        SelectorThreadGroup stg = new SelectorThreadGroup(3);

        //2.把监听的 server 注册到某一个selector上
        stg.bind(9999);

        //测试(通过shell连接本机, vmware虚拟机, .1 是与本机的通讯ip)
        // yum install nc -y
        // yum install nmap -y
        // nc 192.168.25.1 9999
        // 发送数据, 可以发现 Thread-1 after select... size=1 增加并返回数据


    }


}
