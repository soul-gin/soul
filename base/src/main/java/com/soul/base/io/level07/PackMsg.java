package com.soul.base.io.level07;

import java.io.Serializable;

public class PackMsg implements Serializable {

    MyHeader header;

    MyContent body;

    public PackMsg(MyHeader header, MyContent body) {
        this.header = header;
        this.body = body;
    }

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    public MyContent getBody() {
        return body;
    }

    public void setBody(MyContent body) {
        this.body = body;
    }

}
