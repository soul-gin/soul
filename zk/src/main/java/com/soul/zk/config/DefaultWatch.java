package com.soul.zk.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

public class DefaultWatch implements Watcher {

    private CountDownLatch ct;

    public void setCt(CountDownLatch ct) {
        this.ct = ct;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("DefaultWatch event=" + event);

        switch (event.getState()) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                //保证zk连接成功后, ct在进行减减操作
                ct.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
            default:
                System.out.println("null state");
        }
    }
}
