package com.soul.base.io.level07;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * 1. 假设一个需求, 写一个RPC
 * 2. 来回通讯, 连接数量, 拆包
 * 3. 动态代理, 序列化, 协议封装
 * 4. 连接池
 * 5. 就像调用本地方法一样去调用远程方法, 面向java就是所谓的面向interface编程
 */
public class MyRPCTest {

    //模拟server端
    @Test
    public void startServer(){

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup work = boss;
        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        System.out.println("server accept client port=" + channel.remoteAddress().getPort());
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new ServerRequestHandler());
                    }
                }).bind(new InetSocketAddress("localhost", 9090));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //模拟consumer端
    @Test
    public void getCarInfo(){
        new Thread(() -> {
            startServer();
        }).start();

        System.out.println("server started......");

        //动态代理实现
        Car car = proxyGet(Car.class);
        car.getCarInfo("hello");

        //模拟并发情况下, 可能会出现header切割异常问题, 需要处理
        int size = 60;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car2 = proxyGet(Car.class);
                car2.getCarInfo("hello");
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Fly fly = proxyGet(Fly.class);
//        fly.getFlyInfo("hello");
    }

    @SuppressWarnings("unchecked")
    public static <T>T proxyGet(Class<T> interfaceInfo){
        //可以使用cglib 或 jdk动态代理
        ClassLoader loader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};

        return (T)Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //如何设计我们的consumer对于provider的调用过程

                //1. 调用服务, 方法, 参数 -> 封装成msg
                String name = interfaceInfo.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                MyContent content = new MyContent();
                content.setName(name);
                content.setMethodName(methodName);
                content.setParameterTypes(parameterTypes);
                content.setArgs(args);

                //通过内存buffer类来序列化
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                ObjectOutputStream oOut = new ObjectOutputStream(bOut);
                oOut.writeObject(content);
                byte[] msgBody = bOut.toByteArray();

                //2. requestId+msg, 本地需要缓存
                // 协议: header + msgBody
                MyHeader header = createHeader(msgBody);
                //清理buffer, 以便处理header
                bOut.reset();
                //注意: ObjectOutputStream 需要新创建
                oOut = new ObjectOutputStream(bOut);
                oOut.writeObject(header);
                byte[] msgHeader = bOut.toByteArray();
                System.out.println("msgHeader length=" + msgHeader.length);

                //3. 获取连接池
                //获取连接过程: 开始-创建新的  过程-直接获取已创建的
                ClientFactory factory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));

                //4. 发送 -> 走IO
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);

                CountDownLatch cd = new CountDownLatch(1);
                long id = header.getRequestID();
                ResponseHandler.addCallBack(id, new Runnable() {
                    @Override
                    public void run() {
                        //接受到返回数据就放行 cd
                        cd.countDown();
                    }
                });

                byteBuf.writeBytes(msgHeader);
                byteBuf.writeBytes(msgBody);
                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                //io是双向的, sync仅仅阻塞至数据发送完成, 并不会等待至数据接收
                channelFuture.sync();

                //这里先不做超时限制
                cd.await();

                //5. 如果走IO, 未来回来了, 怎么将代码执行到这里
                //(睡眠/回调, 如何让线程停下来?还能让线程继续)

                return null;
            }
        });

    }

    public static MyHeader createHeader(byte[] msg) {
        MyHeader header = new MyHeader();
        int size = msg.length;
        long requestId = Math.abs(UUID.randomUUID().getLeastSignificantBits());

        //自定义标志位
        //int类型4字节, 32bit位, 0000 0000 -> 0001 0100 -> 十六进制的14(0x14)
        int flag = 0x14141414;
        header.setFlag(flag);
        header.setDataLen(size);
        header.setRequestID(requestId);
        return header;
    }

}
