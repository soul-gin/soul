package com.soul.base.io.level07;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseHandler {

    static ConcurrentHashMap<Long, Runnable> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestID, Runnable cb){
        mapping.putIfAbsent(requestID, cb);
    }

    public static void runCallBack(long requestID){
        Runnable runnable = mapping.get(requestID);
        runnable.run();
        removeCB(requestID);
    }

    private static void removeCB(long requestID) {
        mapping.remove(requestID);
    }

}
