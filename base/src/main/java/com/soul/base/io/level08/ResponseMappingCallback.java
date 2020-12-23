package com.soul.base.io.level08;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseMappingCallback {

    static ConcurrentHashMap<Long, CompletableFuture<String>> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestID, CompletableFuture<String> cf){
        mapping.putIfAbsent(requestID, cf);
    }

    public static void runCallBack(PackMsg resp){
        CompletableFuture<String> cf = mapping.get(resp.header.getRequestID());
        cf.complete(resp.body.getRes());
        removeCB(resp.header.getRequestID());
    }

    private static void removeCB(long requestID) {
        mapping.remove(requestID);
    }

}
