package com.soul.base.io.level06;

import io.netty.channel.*;

// 处理 MyInHandler is not a @Sharable handler, so can't be added or removed multiple times.
// 让读写数据的handler变成可共享的, 以便多个client可以连接同一个server的端口
// 这里定义的是一个公用的handler, handler责任链的第一环
@ChannelHandler.Sharable
public class MyChannelInit extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline p = client.pipeline();
        //handler责任链第二环
        //实际上每个客户端的处理器应该自己去创建, 而不是去共享别人的handler
        //MyChannelInit被register时, 触发channelRead
        // 这时 ChannelPipeline 先把自定义的 MyInHandler 放到最后
        p.addLast(new MyInHandler());
        //MyChannelInit主要是为了初始化自定义的MyInHandler, 所以使用完毕可以清理掉(过河拆桥)
        ctx.pipeline().remove(this);

    }
}