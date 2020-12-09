package com.soul.base.io.level07;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ClientFactory {

    int poolSize = 5;

    Random rand = new Random();

    NioEventLoopGroup clientWorker;

    private ClientFactory() {
    }

    private static final ClientFactory factory;

    static{
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    //一个consumer可以连接很多的provider
    //每一个provider都有自己的的pool, K,V

    ConcurrentHashMap<InetSocketAddress, ClientPool> outBoxs = new ConcurrentHashMap<>();


    public synchronized NioSocketChannel getClient(InetSocketAddress address) {

        ClientPool clientPool = outBoxs.get(address);
        if (clientPool == null){
            outBoxs.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outBoxs.get(address);
        }

        int i = rand.nextInt(poolSize);
        NioSocketChannel client = clientPool.clients[i];
        if (client != null && client.isActive()){
            return client;
        }

        synchronized (clientPool.lock[i]){
            client = create(address);
            return client;
        }

    }

    private NioSocketChannel create(InetSocketAddress address) {
        //基于netty的客户端
        clientWorker = new NioEventLoopGroup(1);

        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(clientWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline p = nioSocketChannel.pipeline();
                        //处理: 1. 解决给谁, 2.数据处理
                        p.addLast(new ClientResponses());
                    }
                }).connect(address);

        try {
            NioSocketChannel client = (NioSocketChannel)connect.sync().channel();
            return client;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }
}
