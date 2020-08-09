package com.soul.kafka.level04;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserDefinePartitioner implements Partitioner {
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public int partition(String topic,
                         Object key,
                         byte[] keyBytes,
                         Object value,
                         byte[] valueBytes,
                         Cluster cluster) {
        //返回自定义分区号

        //获取可用分区数
        int numPartitions = cluster.partitionsForTopic(topic).size();
        //没有key, 使用 counter 协助计数, 并对 numPartitions 取模,
        if (keyBytes == null || keyBytes.length == 0) {
            //负数 & Integer.MAX_VALUE 就可以得到正数
            return counter.addAndGet(1) & Integer.MAX_VALUE % numPartitions;
        } else {

            return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
        }
    }

    @Override
    public void close() {
        //关闭生产者的回调方法
        System.out.println("close");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        System.out.println("configure");
    }
}
