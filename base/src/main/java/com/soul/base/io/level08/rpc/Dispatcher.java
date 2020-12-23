package com.soul.base.io.level08.rpc;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于服务端设置全局对象
 */
public class Dispatcher {

    private static Dispatcher dis = null;

    static {
        dis = new Dispatcher();
    }

    public static Dispatcher getDis(){
        return dis;
    }

    private Dispatcher(){

    }

    public static ConcurrentHashMap<String, Object> invokeMap = new ConcurrentHashMap<>();

    public void register(String key, Object val){
        invokeMap.put(key, val);
    }

    public Object get(String key){
        return invokeMap.get(key);
    }

}
