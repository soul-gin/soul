package com.soul.base.io.level07;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;


public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    //provider端(server端)
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        ByteBuf sendBuf = buf.copy();


        //至少保证header是有的(计算字节数, 一个int, 两个long (4+8+8) * 8bit = 160 bit位)
        //实际经过序列化会有一定变化, 通过byte[]的length属性查看
        if (buf.readableBytes() >= 103){
            byte[] bytes = new byte[103];
            buf.readBytes(bytes);
            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            ObjectInputStream oIn = new ObjectInputStream(bIn);
            MyHeader header = (MyHeader)oIn.readObject();

            //获取数据长度, 以便确定需要如何读取
            System.out.println("dataLength=" + header.getDataLen() + " requestID=" + header.getRequestID());
            if (buf.readableBytes() >= header.getDataLen()){
                byte[] data = new byte[(int)header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream bIn2 = new ByteArrayInputStream(data);
                ObjectInputStream oIn2 = new ObjectInputStream(bIn2);
                MyContent body = (MyContent)oIn2.readObject();
                System.out.println("methodName=" + body.getMethodName());
            }

        }

        //先直接原样返回
        ChannelFuture channelFuture = ctx.writeAndFlush(sendBuf);
        channelFuture.sync();

    }
}