package com.soul.jmh.test;

import org.openjdk.jmh.annotations.*;

import static org.junit.jupiter.api.Assertions.*;

public class PSTest {
    /*
    直接右键, 点击run即可
    会报出: trying to acquire the JMH lock (C:\WINDOWS\/jmh.lock): 拒绝访问
    如不指定 temp 目录, jmh会默认使用 C:\WINDOWS , 而windows系统的 C:\WINDOWS 目录需要权限较高
    点击修改运行时参数: Edit Configurations -> Environment Variable -> Include system environment variables
    这样可以把获取锁的目录使用系统安全系数较低的 temp 目录
     */
    //Benchmark 表示测试哪一段代码
    @Benchmark
    //Warmup 预热，由于JVM中对于特定代码会存在优化（本地化 JIT编译），预热对于测试结果很重要
    // iterations 表示调用次数, 默认1次; time 表示调用完成后等几秒, 目前3秒
    @Warmup(iterations = 1, time = 3)
    //Mesurement 总共执行多少次测试, iterations配置次数, time调用完后等待时间
    @Measurement(iterations = 1, time = 3)
    //Timeout

    //Threads 线程数，由fork指定, 目前起5个线程测试
    @Fork(5)
    //Benchmark mode 基准测试的模式
    //Throughput 表示"吞吐量模式", 计算每秒执行多少次
    @BenchmarkMode(Mode.Throughput)
    public void testForEach() {
        PS.foreach();
    }

    //报告
    //Benchmark            Mode  Cnt  Score   Error  Units
    //PSTest.testForEach  thrpt    5  0.829 ± 0.167  ops/s
    //Score 在 Throughput 模式下, 表示每秒可以执行方法多少次, 和机器性能相关: 目前是 0.829
}