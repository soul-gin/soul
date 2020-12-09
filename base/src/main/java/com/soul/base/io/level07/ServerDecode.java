package com.soul.base.io.level07;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class ServerDecode extends ByteToMessageDecoder {

    //本身继承了 ChannelInboundHandlerAdapter, 所以也是channel处理的责任链的一环
    //父类里一定有channelread { 执行前拼接老的截断buf decode() 执行后将剩余被截断的buf留存; 对out遍历存储完整数据 } -> bytebuf ()
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> out) throws Exception {
        System.out.println("channel read start=" + buf.readableBytes());
        //至少保证header是有的(计算字节数, 一个int, 两个long (4+8+8) * 8bit = 160 bit位)
        //实际经过序列化会有一定变化, 通过byte[]的length属性查看
        while (buf.readableBytes() >= 103){
            byte[] bytes = new byte[103];
            //注意, 为了处理body可能不完整, 需要将header+body一起留到下次处理
            //所以header的读取不能移动指针
            buf.getBytes(buf.readerIndex(), bytes);
            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            ObjectInputStream oIn = new ObjectInputStream(bIn);
            MyHeader header = (MyHeader)oIn.readObject();

            //获取数据长度, 以便确定需要如何读取
            System.out.println("server dataLength=" + header.getDataLen() + " requestID=" + header.getRequestID());
            if (buf.readableBytes() >= header.getDataLen()){
                //特殊处理header指针问题, 移动指针至header结束的位置
                buf.readBytes(103);

                //读取
                byte[] data = new byte[(int)header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream bIn2 = new ByteArrayInputStream(data);
                ObjectInputStream oIn2 = new ObjectInputStream(bIn2);
                MyContent body = (MyContent)oIn2.readObject();
                System.out.println("methodName=" + body.getMethodName());

                //完整数据的存储
                out.add(new PackMsg(header, body));

            } else {
                //如果body被切割了
                System.out.println("server body not enough, length=" + buf.readableBytes());
                //直接跳出, 留存
                break;
            }

        }
    }

}
