package com.soul.base.io.level06;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.net.InetSocketAddress;

public class OfficialNetty {

    @Test
    public void nettyClient(){
        //测试: 在 192.168.25.101 的shell界面使用nc创建服务端:
        // nc -l 192.168.25.101 9090

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        p.addLast(new MyInHandler());
                    }
                })
                .connect(new InetSocketAddress("192.168.25.101", 9090));
        try {
            //阻塞至连接成功
            Channel client = connect.sync().channel();

            //写数据
            ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
            ChannelFuture send = client.writeAndFlush(buf);
            send.sync();

            //阻塞至连接断开
            client.closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void nettyClient2(){
        //测试: 在 192.168.25.101 的shell界面使用nc创建服务端:
        // nc -l 192.168.25.101 9090

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(group)
                .channel(NioSocketChannel.class)
                .handler(new MyChannelInit())
                .connect(new InetSocketAddress("192.168.25.101", 9090));
        try {
            //阻塞至连接成功
            Channel client = connect.sync().channel();

            //写数据
            ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
            ChannelFuture send = client.writeAndFlush(buf);
            send.sync();

            //阻塞至连接断开
            client.closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void nettyServer(){
        //测试: 在 192.168.25.101 的shell界面使用nc创建客户端:
        // nc 192.168.25.1 9090

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(group, group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new MyInHandler());
                    }
                })
                .bind(new InetSocketAddress("192.168.25.1", 9090));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nettyServer2(){
        //测试: 在 192.168.25.101 的shell界面使用nc创建客户端:
        // nc 192.168.25.1 9090

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(group, group)
                .channel(ServerSocketChannel.class)
                .childHandler(new MyChannelInit())
                .bind(new InetSocketAddress("192.168.25.1", 9090));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
