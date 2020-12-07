package com.soul.base.io.level07;

import io.netty.channel.socket.nio.NioSocketChannel;


public class ClientPool {

    NioSocketChannel[] clients;

    Object[] lock;


    public ClientPool(int size) {
        clients = new NioSocketChannel[size];
        //锁可以直接初始化
        lock = new Object[size];
        for (int i = 0; i < size; i++) {
            lock[i] = new Object();
        }
    }





}
