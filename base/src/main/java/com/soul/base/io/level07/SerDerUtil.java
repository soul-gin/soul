package com.soul.base.io.level07;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SerDerUtil {

    static ByteArrayOutputStream bOut = new ByteArrayOutputStream();

    //序列化, 会存在并发问题
    public synchronized static byte[] ser(Object msg) {
        //清空, 便于重复利用
        bOut.reset();
        byte[] bytesMsg = null;
        ObjectOutputStream oOut = null;
        try {

            oOut = new ObjectOutputStream(bOut);
            oOut.writeObject(msg);
            bytesMsg = bOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesMsg;
    }

}
