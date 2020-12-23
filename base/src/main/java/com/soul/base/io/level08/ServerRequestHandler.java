package com.soul.base.io.level08;

import com.soul.base.io.level08.rpc.Dispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;


public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    Dispatcher dis;

    public ServerRequestHandler(Dispatcher dis) {
        this.dis = dis;
    }

    //provider端(server端)
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        PackMsg reqPackMsg = (PackMsg) msg;

        //关注rpc通讯协议, 来的时候flag 是 0x14141414
        //返回会有新的header+body
        //1. 可以直接在当前方法中处理IO业务和返回
        //2. 也可以使用netty自己的eventloop来处理业务及返回
        String ioThreadName = Thread.currentThread().getName();
        //复用netty的线程池
        ctx.executor().execute(new Runnable() {
        //或者使用netty单独的IO线程处理
        //ctx.executor().parent().next().execute(new Runnable() {
            @Override
            public void run() {
                //获取接口名称, 调用方法名
                String serviceName = reqPackMsg.body.getName();
                String methodName = reqPackMsg.body.getMethodName();
                //获取调用对象, 方法 进行调用处理
                Object c = dis.get(serviceName);
                Class<?> cClass = c.getClass();
                Object res = null;
                try {
                    Method method = cClass.getMethod(methodName, reqPackMsg.body.getParameterTypes());
                    res = method.invoke(c, reqPackMsg.body.getArgs());

                } catch (Exception e) {
                    e.printStackTrace();
                }


                //String execThreadName = Thread.currentThread().getName();

                MyContent body = new MyContent();
                //String res = "ioThreadName=" + ioThreadName + " execThreadName=" + execThreadName + " from args:" + reqPackMsg.body.getArgs()[0];
                body.setRes((String)res);
                body.setMethodName(reqPackMsg.body.getMethodName());
                //序列化
                byte[] bodyByte = SerDerUtil.ser(body);

                MyHeader header = new MyHeader();
                header.setRequestID(reqPackMsg.header.getRequestID());
                //返回的码值
                header.setFlag(0x14141424);
                header.setDataLen(bodyByte.length);
                //序列化
                byte[] headerByte = SerDerUtil.ser(header);
                //System.out.println("server header.length=" + headerByte.length);

                //转换成字节数组返回
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerByte.length + bodyByte.length);
                byteBuf.writeBytes(headerByte);
                byteBuf.writeBytes(bodyByte);

                ChannelFuture channelFuture = ctx.writeAndFlush(byteBuf);
                try {
                    channelFuture.sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

}