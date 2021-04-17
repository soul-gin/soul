package com.soul.base.juc.level08;

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
     * cpu 核数: Runtime.getRuntime().availableProcessors()
     */
    private static final int DEFAULT_MAX_CONCURRENT = 1;

    private static final String THREAD_POOL_NAME = "Risk-";

    private static final ThreadFactory FACTORY = new NameDaemonThreadFactory(THREAD_POOL_NAME, true);

    /**
     * 队列大小, LinkedBlockingQueue 添加和删除两把锁是分开的，竞争会小一些
     */
    private static final int DEFAULT_SIZE = 500;
    private static final LinkedBlockingQueue<Runnable> EXECUTE_QUEUE = new LinkedBlockingQueue<>(DEFAULT_SIZE);

    /**
     * 线程存活时间, 0L 不释放, 60L
     */
    private static final long DEFAULT_KEEP_ALIVE = 0L;

    /**
     * {@link ExecutorService}
     */
    private static final ExecutorService EXECUTOR;

    private ThreadPoolUtil() {
    }

    static {
        // 创建 Executor
        // 最大线程, 目前设置为初始线程的 4 倍
        try {

            EXECUTOR = new ThreadPoolExecutor(DEFAULT_MAX_CONCURRENT,
                    DEFAULT_MAX_CONCURRENT,
                    DEFAULT_KEEP_ALIVE,
                    TimeUnit.SECONDS,
                    EXECUTE_QUEUE,
                    FACTORY,
                    new ThreadPoolExecutor.AbortPolicy());

            // 可选, 注册线程池关闭事件的挂钩
            Runtime.getRuntime().addShutdownHook(
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //开始执行关闭逻辑
                                    ThreadPoolUtil.LOGGER.info(THREAD_POOL_NAME + " shutting down...");
                                    EXECUTOR.shutdown();
                                    try {
                                        // 还有等待队列, 等待3秒执行关闭
                                        if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                                            //关闭异常
                                            ThreadPoolUtil.LOGGER.error(THREAD_POOL_NAME + " shutdown immediately due to wait timeout...");
                                            EXECUTOR.shutdownNow();
                                        }
                                    } catch (InterruptedException e) {
                                        //关闭异常
                                        ThreadPoolUtil.LOGGER.error(THREAD_POOL_NAME + " shutdown interrupted...");
                                        EXECUTOR.shutdownNow();
                                    }
                                    //正常关闭完成
                                    ThreadPoolUtil.LOGGER.info(THREAD_POOL_NAME + " shutdown complete...");
                                }
                            }
                    )
            );
        } catch (Exception e) {
            LOGGER.error(THREAD_POOL_NAME + " init error.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 提供 线程池对象
     * @return 线程池对象
     */
    public static ExecutorService getExecutor() {
        return EXECUTOR;
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
            EXECUTOR.execute(task);
        } catch (RejectedExecutionException e) {
            LOGGER.error(THREAD_POOL_NAME + " Task executing was rejected.", e);
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
            return EXECUTOR.submit(task);
        } catch (RejectedExecutionException e) {
            LOGGER.error(THREAD_POOL_NAME + " Task executing was rejected.", e);
            throw new UnsupportedOperationException(THREAD_POOL_NAME + " Unable to submit the task, rejected.", e);
        }
    }
}