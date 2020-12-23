package com.soul.base.io.level08.proxy;

import com.soul.base.io.level08.ClientFactory;
import com.soul.base.io.level08.MyContent;
import com.soul.base.io.level08.MyHeader;
import com.soul.base.io.level08.ResponseMappingCallback;
import com.soul.base.io.level08.rpc.Dispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 代理rpc调用
 *
 * FC 函数调用, 本机寻址调起(function call)
 * SC 用户空间至内核空间调用操作, 0x80软中断, 系统调用(system call)
 * RPC 远程调用, socket (remote process call)
 * IPC 管道, 信号, socket
 */
public class MyProxy {

    @SuppressWarnings("unchecked")
    public static <T>T proxyGet(Class<T> interfaceInfo){
        //可以使用cglib 或 jdk动态代理
        ClassLoader loader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};

        //获取全局变量map
        Dispatcher dis = Dispatcher.getDis();

        //这里最好能判断调用的是远程的方法(REMOTE) 还是 本机就有的方法(LOCAL)
        //需要用到 Dispatcher, 本地则直接返回缓存的对象
        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object res = null;
                //如何设计我们的consumer对于provider的调用过程
                Object o = dis.get(interfaceInfo.getName());
                if(null == o){
                    //本地没有缓存对应服务提供对象, 走rpc
                    //1. 调用服务, 方法, 参数 -> 封装成msg
                    //可以通过接口名去 Dispatcher 中查找, 确定是本地调用还是远程调用
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
                    //测试header大小, header大小一般协议固定
                    //System.out.println("msgHeader length=" + msgHeader.length);

                    //3. 获取连接池
                    //获取连接过程: 开始-创建新的  过程-直接获取已创建的
                    ClientFactory factory = ClientFactory.getFactory();
                    NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));

                    //4. 发送 -> 走IO
                    ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);

                    long id = header.getRequestID();
                    CompletableFuture<String> resp = new CompletableFuture<>();
                    ResponseMappingCallback.addCallBack(id, resp);

                    byteBuf.writeBytes(msgHeader);
                    byteBuf.writeBytes(msgBody);
                    ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                    //io是双向的, sync仅仅阻塞至数据发送完成, 并不会等待至数据接收
                    channelFuture.sync();


                    //5. 如果走IO, 未来回来了, CompletableFuture 通过complete设置, get获取
                    //resp.complete(设置值)
                    res = resp.get();
                } else {
                    //本地有缓存, 走本地调用即可
                    //可以插入一些埋点监控
                    Class<?> cClass = o.getClass();
                    try {
                        //获取接口名称, 调用参数类型
                        Method m = cClass.getMethod(method.getName(), method.getParameterTypes());
                        res = m.invoke(o, args);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return res;
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
