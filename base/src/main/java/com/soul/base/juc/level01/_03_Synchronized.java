package com.soul.base.juc.level01;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
- 锁: 并发情况下, 处理线程间共享资源同步
synchronized hotspot实现(没有规范,不同虚拟机实现可以不同):
 在锁对象的头上(64位,前两位(mark word)标记是否被锁定);
 当第一个线程过来时: 偏向锁->锁偏向第一个线程(乐观锁,认为不会有并发,并未真正加锁,只是记录了第一个线程对象的id);
 当来了第二个线程,锁升级为自旋锁(cas): 第二个线程默认自动循环等待10次(自己转圈玩),不去竞争锁的权限(这时候第一个线程释放锁,那么第二个线程获取执行权限);
 10次以后,锁升级为重量级锁,这时候,第二个线程就会向cpu申请锁了(申请CPU执行权限);
 锁只能升级,不能降级,所有会出现synchronized被多个线程竞争后,升级为重量级锁,即便后续只有一个线程执行了,也不知偏向锁,而是重量级锁了;

 自旋锁,是线程自己循环,不经过linux系统的内核,即无用户态向内核态的切换
 适合执行时间短,线程少的场景(执行时间长则会导致长时间大量自旋等待;线程太多,几万个锁自旋cpu扛不住)
 执行时间长,线程数量多,用系统锁(synchronized 重量级锁, 线程会等待,不像自旋会消耗cpu资源)

 注意: synchronized 不能使用 String(可能和别人代码同一个锁,出现死锁(不同线程),或重入(同一个线程)问题)
 Integer(改变值会变成新的对象, 锁就成多把了), Long这些基础数据类型类库

 特点:
 可重入: 可以重入被同一个锁锁定的 synchronized 方法(本类中方法, 父类方法), 如果不能重入就死锁了
 锁升级(无锁 -> 轻 -> 重; 乐观 -> 悲观; 不会降级;)
| 存储内容 | 标志位 | 描述 |
| :-----| :----: | :----|
| 对象哈希码、对象分代年 | 01 | 未锁定 |
| 偏向线程ID | 01 | 偏向锁 |
| 指向锁记录的指针 | 00 | 轻量级锁 |
| 指向重量级锁的指针 | 10 | 重量级锁 |
| 空 | 11 | GC标记 |


volatile
1.保障线程可见性
 - MESI
 - 缓存一致性协议
2.禁止指令重排序(防止读取到刚初始化的值)
 - DCL单例
 - Double check lock
 - load fence / store fence 读写屏障(保障命令前的读/写操作全处理完成再继续后续操作)

 cas(无锁优化 自旋)
 Compare And Set
 cas(V, Expected, NewValue)
 (V-要改的值, Expected-期望当前要被改的值执行cas时查询到(get)的是多少, NewValue-需要将要改的值被设定成的值)
 - if V == E
   V = New
   otherwise try again or fail

 假设要将一个a=0 执行 a=a+1
 那么执行操作时 V=0 , E=0 , NEW=1 则能修改成功
 如果执行操作时 V=0 , E=2 , NEW=1 则表示V被其他线程修改了, V != E 则重新获取参数并执行方法(V=2 , E=2 , NEW=3)

 ABA问题, 主要针对包装类型, 基础类型(基础类型的包装类型)无所谓; 你的女朋友跟别人跑了, 又回来找你了, 已经不是原来那个女朋友了
 原因: 包装类型只比较了地址值(而对象的属性值补比较, 实际可能已经被其他线程修改了)
 线程1:  设置流程: filed = A -> filed = B -> filed = A
 线程2:  设置流程: A(A里面某些属性值被修改, 婚姻状态-离婚)
 假设: 线程1执行到 filed = B 时, 线程2改了A的值, 线程1原本想将 filed 设置成最开始的A(没改婚姻状态的), 但实际设置的是A(婚姻状态-离婚)


 */
public class _03_Synchronized {

    // volatile 只是保证可见, 并未对数据加锁
    private static volatile int withoutSyncCount = 20;
    // 使用 AtomicInteger 可以保证共享变量 withoutSyncCount 正常( Atomic 为 cas 锁实现 )
    // 不过打印非同步, 依然存在问题
    //private static AtomicInteger withoutSyncCount = new AtomicInteger(20);

    //静态方法 synchronized 公用变量
    private static int withStaticSyncCount = 20;

    //普通方法 synchronized 公用变量
    private static int withSyncCount = 100;
    // 使用 AtomicInteger 可以保证共享变量 withSyncCount 正常( Atomic 为 cas 锁实现 )
    // 不过打印非同步, 依然存在问题
    //private static AtomicInteger withSyncCount = new AtomicInteger(100);


    public static void main(String[] args) {
        try {
            //未加同步
            testSync(WithoutSync.class);
            Thread.sleep(500);
            System.out.println("WithoutSync end, t1.withoutSyncCount=" + withoutSyncCount);
            System.out.println("--------");

            //静态方法synchronized同步, class锁,一把,可以锁定
            WithStaticSync t2 = new WithStaticSync();
            testSync(WithStaticSync.class);
            Thread.sleep(500);
            System.out.println("WithStaticSync end, t2.withoutSyncCount=" + withStaticSyncCount);
            System.out.println("--------");

            //普通方法synchronized同步, this锁,两把,无法锁定
            testSync(WithSync.class);
            Thread.sleep(1600);
            System.out.println("WithSync end, t3.withoutSyncCount=" + withSyncCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSync(Class<? extends Runnable> clazz) throws Exception {
        //创建线程
        List<Thread> threadList = new LinkedList<>();

        // 方式一
        // 注意这里创建了两个实现 Runnable 的对象, 所以使用 synchronized 的this锁(多把锁)会失效
        // 使用 static synchronized 的 class 锁可以正常锁住(一把锁)
        for (int i = 0; i < 2; i++) {
            Runnable t = clazz.newInstance();
            threadList.add(new Thread(t, "Thread" + i));
        }

        // 方式二
        /*
        //这样 this 锁也能生效,因为只创建了一个 Runnable 对象
        Runnable t = clazz.newInstance();
        for (int i = 0; i < 2; i++) {
            threadList.add(new Thread(t, "Thread" + i));
        }*/

        //启动线程
        threadList.forEach(Thread::start);
    }


    //代码块未加锁
    static class WithoutSync implements Runnable {

        public void run() {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(5);
                    //这里可能会导致数据最终不为 0
                    withoutSyncCount--;
                    //withoutSyncCount.getAndDecrement();
                    Thread.sleep(5);
                    //这里会导致打印的可能是其他线程修改的值
                    System.out.println(Thread.currentThread().getName() + " count1= " + withoutSyncCount);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class WithStaticSync implements Runnable {
        public void run() {
            countSync();
        }

        //这里等同于synchronized(WithStaticSync.class)
        private static synchronized void countSync() {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(5);
                    withStaticSyncCount--;
                    Thread.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " count2= " + withStaticSyncCount);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class WithSync implements Runnable {
        //使用this当前对象锁, 如果 WithSync 创建了多个则this锁会失效
        public synchronized void run() {
            // synchronized锁 可以重入
            testReentry();
        }

        private synchronized void testReentry() {
            try {
                for (int i = 0; i < 50; i++) {
                    //Random random = new Random();
                    //int nextInt = random.nextInt(4) + 1;
                    Thread.sleep(2);
                    withSyncCount--;
                    //withSyncCount.getAndDecrement();
                    Thread.sleep(5);
                    System.out.println(Thread.currentThread().getName() + " count3= " + withSyncCount);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
