package com.soul.base.io.level07;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于服务端设置全局对象
 */
public class Dispatcher {

    public static ConcurrentHashMap<String, Object> invokeMap = new ConcurrentHashMap<>();

    public void register(String key, Object val){
        invokeMap.put(key, val);
    }

    public Object get(String key){
        return invokeMap.get(key);
    }

}
