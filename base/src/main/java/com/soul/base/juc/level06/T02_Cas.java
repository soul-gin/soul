package com.soul.base.juc.level06;


public class T02_Cas {

    //使用 enum 来避免取其他值
    enum ReadyToRun {T1, T2}

    //思考为什么必须volatile
    static volatile ReadyToRun r = ReadyToRun.T1;

    public static void main(String[] args) {

        char[] aI = "1234567".toCharArray();
        char[] aC = "ABCDEFG".toCharArray();

        new Thread(() -> {

            for (char c : aI) {
                while (r != ReadyToRun.T1) {}
                System.out.print(c);
                r = ReadyToRun.T2;
            }

        }, "t1").start();

        new Thread(() -> {

            for (char c : aC) {
                while (r != ReadyToRun.T2) {}
                System.out.print(c);
                r = ReadyToRun.T1;
            }
        }, "t2").start();
    }
}


