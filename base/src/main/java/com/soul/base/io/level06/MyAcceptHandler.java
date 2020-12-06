package com.soul.base.io.level06;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;


public class MyAcceptHandler extends ChannelInboundHandlerAdapter {

    EventLoopGroup selector;

    ChannelHandler handler;

    public MyAcceptHandler(EventLoopGroup thread, ChannelHandler handler) {
        this.selector = thread;
        this.handler = handler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx){
        System.out.println("client register...");

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client active...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //netty处理了accept
        //这里的msg是client的连接
        SocketChannel client = (SocketChannel) msg;


        //2.响应式 handler
        ChannelPipeline p = client.pipeline();
        //handler责任链第一环
        //ChannelPipeline中放的第一个handler是 MyChannelInit
        p.addLast(handler);

        //1. 注册
        selector.register(client);

    }

}
