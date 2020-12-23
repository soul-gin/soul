package com.soul.base.io.level08;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientResponses extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //返回数据
        PackMsg respPackMsg = (PackMsg) msg;

        //处理返回数据
        ResponseMappingCallback.runCallBack(respPackMsg);

    }
}
