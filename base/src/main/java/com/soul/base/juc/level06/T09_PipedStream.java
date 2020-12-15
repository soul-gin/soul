package com.soul.base.juc.level06;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class T09_PipedStream {


    public static void main(String[] args) throws Exception {
        char[] aI = "1234567".toCharArray();
        char[] aC = "ABCDEFG".toCharArray();

        //线程之间管道同步, 效率较低(内部同步方法很多), 稍微了解即可
        PipedInputStream input1 = new PipedInputStream();
        PipedInputStream input2 = new PipedInputStream();
        PipedOutputStream output1 = new PipedOutputStream();
        PipedOutputStream output2 = new PipedOutputStream();

        //线程1的输入连接线程2的输出
        //线程2的输入连接线程1的输出
        //相当于两个线程可以互相发送消息(线程间消息通讯)
        input1.connect(output2);
        input2.connect(output1);

        String msg = "Your Turn";



        new Thread(() -> {

            byte[] buffer = new byte[9];

            try {
                for(char c : aI) {
                    input1.read(buffer);

                    //判断读取到的消息是否为可以执行
                    if(new String(buffer).equals(msg)) {
                        System.out.print(c);
                    }

                    output1.write(msg.getBytes());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }, "t1").start();

        new Thread(() -> {

            byte[] buffer = new byte[9];

            try {
                for(char c : aC) {

                    System.out.print(c);

                    output2.write(msg.getBytes());

                    input2.read(buffer);

                    if(new String(buffer).equals(msg)) {
                        continue;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }, "t2").start();
    }
}


