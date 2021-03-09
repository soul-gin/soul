package com.soul.base.juc;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author gin
 * @date 2021/3/9
 */
public class ThreadPoolUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtil.class);

    /**
     * 核心线程数, 来了任务会判断worker数量是否小于核心线程数, 小于则创建新线程
     * 目前设置为 cpu 核数, 根据实际需求配置(可以初始线程为0)
     */
    private static final int DEFAULT_MAX_CONCURRENT = Runtime.getRuntime()
            .availableProcessors();

    /**
     * 线程池名称格式
     */
    private static final String THREAD_POOL_NAME = "ThreadPoolUtil-%d";

    /**
     * 线程工厂( BasicThreadFactory )
     * <dependency>
     * <groupId>org.apache.commons</groupId>
     * <artifactId>commons-lang3</artifactId>
     * <version>3.6</version>
     * </dependency>
     */
    private static final ThreadFactory FACTORY = new BasicThreadFactory.Builder()
            .namingPattern(THREAD_POOL_NAME)
            .daemon(true).build();

    /**
     * 队列大小
     */
    private static final int DEFAULT_SIZE = 500;

    /**
     * 线程存活时间
     * 归还线程时间(线程没活干了)
     */
    private static final long DEFAULT_KEEP_ALIVE = 60L;

    /**
     * {@link ExecutorService}
     */
    private static volatile ExecutorService executor;

    /**
     * 执行队列
     * ArrayBlockingQueue 基于数组实现的阻塞队列
     * LinkedBlockingQueue 也是有界队列，但是不设置大小时就时Integer.MAX_VALUE，内部是基于链表实现的
     * ArrayBlockingQueue 实现简单，表现稳定，添加和删除使用同一个锁，通常性能不如LinkedBlockingQueue
     * LinkedBlockingQueue 添加和删除两把锁是分开的，所以竞争会小一些
     */
    private static LinkedBlockingQueue<Runnable> executeQueue = new LinkedBlockingQueue<>(DEFAULT_SIZE);

    /**
     * 此类型无法实例化
     */
    private ThreadPoolUtil() {
    }

    static {
        // 创建 Executor
        // 最大线程, 目前设置为初始线程的 4 倍
        try {

            executor = new ThreadPoolExecutor(DEFAULT_MAX_CONCURRENT,
                    DEFAULT_MAX_CONCURRENT * 4,
                    DEFAULT_KEEP_ALIVE,
                    TimeUnit.SECONDS,
                    executeQueue,
                    FACTORY);

            // 可选, 也可以不注册
            // 注册线程池关闭事件的挂钩
            Runtime.getRuntime().addShutdownHook(
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //开始执行关闭逻辑
                                    ThreadPoolUtil.LOGGER.info("ThreadPoolUtil shutting down.");
                                    executor.shutdown();
                                    try {
                                        // 还有等待队列, 等待3秒执行关闭
                                        if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                                            //关闭异常
                                            ThreadPoolUtil.LOGGER.error("ThreadPoolUtil shutdown immediately due to wait timeout.");
                                            executor.shutdownNow();
                                        }
                                    } catch (InterruptedException e) {
                                        //关闭异常
                                        ThreadPoolUtil.LOGGER.error("ThreadPoolUtil shutdown interrupted.");
                                        executor.shutdownNow();
                                    }
                                    //正常关闭完成
                                    ThreadPoolUtil.LOGGER.info("ThreadPoolUtil shutdown complete.");
                                }
                            }
                    )
            );
        } catch (Exception e) {
            LOGGER.error("ThreadPoolUtil init error.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 提供 线程池对象
     * @return 线程池对象
     */
    public static ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 执行任务，不管是否成功
     * {@link ExecutorService#execute(Runnable)}
     *
     * @param task Runnable
     * @return 提交任务是否成功
     */
    public static boolean execute(Runnable task) {
        try {
            executor.execute(task);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Task executing was rejected.", e);
            return false;
        }
        return true;
    }

    /**
     * 提交任务，并可以在稍后获取其执行情况
     * {@link ExecutorService#submit(Callable)}
     *
     * @param task Callable
     * @return 提交任务是否成功
     */
    public static <T> Future<T> submit(Callable<T> task) {
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Task executing was rejected.", e);
            throw new UnsupportedOperationException("Unable to submit the task, rejected.", e);
        }
    }
}