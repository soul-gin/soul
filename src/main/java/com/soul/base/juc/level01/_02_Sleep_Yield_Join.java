package com.soul.base.juc.level01;

/**
 *
**线程状态
 - 新建状态(NEW):
 建立一个线程对象后, 该线程对象就处于新建状态. 它保持这个状态直到程序 start() 这个线程.
 - 就绪状态(Ready):
 当线程对象调用了start()方法之后, 该线程就进入就绪状态. 就绪状态的线程处于就绪队列中, 争抢CPU执行时间片段的权限
 - 运行状态(Running):
 如果就绪状态的线程获取 CPU 执行权限, 就可以执行 run(), 此时线程便处于运行状态.
 处于运行状态的线程可以变为:
 阻塞状态:
 就绪状态: yield(); 线程被挂起(每个线程只会获得短暂CPU时间,时间片段内未执行完成则会被挂起,等待CPU下一次调度)
 死亡状态: 线程执行完成; stop()不建议使用,容易产生状态不一致(调用stop线程就销毁了,非正常处理完所有操作后结束,不能确定线程中的数据处理(改变)到什么值了)

 - 等待阻塞(TimeWaiting)：
 sleep(time); wait(time); join(time); LockSupport.parkNanos(); LockSupport.parkUntil();
 时间结束回到就绪状态(interrupt 可以打断等待,需catch异常,一般较少使用)
 - 其他阻塞(Waiting)：
 wait(); join(); LockSupport.park(); 需 notify(); notifyAll(); LockSupport.unPark(); 解除阻塞
 - 同步阻塞(Blocked)：线程在执行同步代码块时获取锁失败.

 - 死亡状态(Terminated):
 一个运行状态的线程完成任务或者其他终止条件发生时, 该线程就切换到终止状态.
 *
 */
public class _02_Sleep_Yield_Join {
    public static void main(String[] args) {
        testSleep();
        testYield();
        testJoin();
    }

    /**
     * 线程进入阻塞状态, 等睡眠时间到了就恢复就绪状态, 可以争抢CPU执行权限
     */
    private static void testSleep() {

        Thread t = new Thread(() -> {
            for (int i = 0; i < 7; i++) {
                System.out.println("Z" + i);
                try {
                    Thread.sleep(10);
                    //TimeUnit.Milliseconds.sleep(10)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //新建
        System.out.println("新建_state=" + t.getState());
        t.start();
        //运行
        System.out.println("运行_state=" + t.getState());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //死亡
        System.out.println("死亡_state=" + t.getState());
    }

    /**
     * 执行了 yield() 方法的线程让出执行权,回到就绪状态,依然具有争抢CPU执行权的权利
     */
    private static void testYield() {
        new Thread(()->{
            for(int i=0; i<10; i++) {
                System.out.println("A" + i);
                if(i%2 == 0) Thread.yield();
            }
        }).start();

        new Thread(()->{
            for(int i=0; i<10; i++) {
                System.out.println("------------B" + i);
                if(i%2 == 0) Thread.yield();
            }
        }).start();
    }

    /**
     * 常用于等待另外一个线程的结束
     * main中起了 t1 t2 t3 ,如何让线程按t3 t2 t1执行?
     * main方法中顺序调用t3.join -> t2.join -> t1.join
     * 或 t1中调用t2.join,t2中调用t3.join
     */
    private static void testJoin() {
        Thread t1 = new Thread(()->{
            for(int i=0; i<10; i++) {
                System.out.println("GIN" + i);
                try {
                    Thread.sleep(50);
                    //TimeUnit.Milliseconds.sleep(500)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(()->{

            try {
                //先等t1完全执行完成
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(int i=0; i<10; i++) {
                System.out.println("SOUL" + i);
                try {
                    Thread.sleep(50);
                    //TimeUnit.Milliseconds.sleep(500)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();
        try {
            //主线程将等待t2执行完成
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("main end...");
    }
}
