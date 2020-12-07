package com.soul.base.io.level07;

import java.io.Serializable;

public class MyHeader implements Serializable {

    /**
     * 通讯上的协议
     * 1. 协议编号
     * 2. UUID 区分不同请求
     * 3. DATA_LEN 请求体长度
     */
    int flag; //32bit可以表示很多信息

    long requestID;

    long dataLen;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }
}
