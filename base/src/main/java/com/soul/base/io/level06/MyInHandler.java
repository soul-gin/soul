package com.soul.base.io.level06;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


//每个客户端自定义的handler
//责任链的第二环
public class MyInHandler extends ChannelInboundHandlerAdapter {

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
        ByteBuf buf = (ByteBuf) msg;
        //会移动指针, 读取后的buf不能再直接读取了
        //CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
        //直接get, 不移动指针, 指定读取区间, 不会移动指针
        CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
        System.out.println("msg=" + str);
        //返回信息
        ctx.writeAndFlush(buf);
    }
}
