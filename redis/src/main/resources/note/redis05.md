
#### 持久化
缓存: 数据可以丢, 速度极快
数据库: 数据绝对不能丢(非服务器断电), 速度+持久性

方案
1. 快照/副本
2. 日志

> 快照: RDB (redis db, 时点性)

> 快照实现方案
> 1. 阻塞, redis不对外提供服务, 将所有的k-v转换成db.file后再提供服务
> 2. 非阻塞, redis对外提供服务, 并将所有k-v转换成db,file(问题点: 提供服务过程中, k-v会对应被更新, 如何处理?)

> 快照原理探究
> 对应linux系统父子进程
> 管道(  |  )
> 1. 衔接, 前一个命令的输出作为后一个命令的输入
> 2. 管道会触发创建子进程

> 子进程不会修改父进程的数据
```shell
[root@gin01 ~]# echo $num
1
[root@gin01 ~]# ((num++))
[root@gin01 ~]# echo $num
2
#这里可以发现子进程不会修改父进程的变量值
[root@gin01 ~]# ((num++)) | echo $num
2
[root@gin01 ~]# echo $num
2

```

> 父进程不会修改子进程的数据
```shell
vim testFork.sh
#!/bin/bash

echo $$
echo $num
#这里可以在主进程查看num是否被子进程修改
num=999
echo "num=$num"
sleep 30
#这里在主进程修改值, 可以查看是否影响子进程
echo "num=$num"

chmod +x testFork.sh
#可以发现父进程的sum没有被子进程修改
[root@kafka01 ~]# num=1
[root@kafka01 ~]# echo $num
1
[root@kafka01 ~]# ./testFork.sh &
[1] 89426
[root@kafka01 ~]# 89426

num=999

#按下回车键后, 查看父进程的num, 发现还是1
[root@kafka01 ~]# echo $num
1
#修改父进程的num值
[root@kafka01 ~]# num=777
[root@kafka01 ~]# echo $num
777
#子进程30s计时结束后, 打印num还是子进程中的999
[root@kafka01 ~]# num=999
^C
[1]+  Done                    ./testFork.sh

```

> | 与 pid
```shell
#输出shell客户端的pid
[root@gin01 ~]# echo $BASHPID
50278
[root@gin01 ~]# echo $BASHPID
50278
#发现输出为子进程
[root@gin01 ~]# echo $BASHPID | more
59521
[root@gin01 ~]# echo $BASHPID | more
59529
[root@gin01 ~]# echo $BASHPID | more
59543
#$$比较特殊, 高于 | 执行
[root@gin01 ~]# echo $$ | more
50278

```

> export的环境变量, 子进程的修改不会破坏父进程,
> 父进程的修改也不会破坏子进程

> 如果父进程是redis(数据比如10GB), 在进程rdb快照操作时:
> 1. 速度是否能满足需求
> 2. 内存空间是否足够(父进程已占用10GB, 服务器还需要10GB创建子进程?) 

> 基于 fork() 系统调用实现:
> 1. 速度快(copy on write原理, 创建子进程并不发生复制, 仅在数据发生修改操作时, 复制被修改的数据部分(理论上不可能在拷贝时数据全都被修改了))
> 2. 空间小(持有对实际物理内存的指针(数据开始位置->结束位置), 不实际使用物理内存存储数据, 需要空间仅fork占用的开始位置指针以及内核进行复制的数据占用的空间, 理论上远小于redis占用空间(上述的10GB))
> 复制过程就是从数据开始位置的指针读取数据, 写入 db.file 文件;期间被修改的数据会留存副本, 当读取到对应位置时, 获取副本值, 写入db.file; 从而完成一次时点性(精确创建fork的时间, 如18:00:00)的快照操作

> redis RDB命令(时点性)
> save 命令, 阻塞方式(除非关机维护会使用)
> bgsave 命令, 触发fork创建子进程, 异步非阻塞
> 配置文件中, 配置标识为save, 实际触发的时bgsave规则
> 弊端:
> 1. 不支持拉链, 只有一个dump.rdb(需要额外维护每天备份)
> 2. 丢失数据相对多一些, 时点与时点之间窗口数据容易丢失
> 优点:
> 1. 存储的数据是二进制字节数据(类似java中的序列化), 恢复的速度相对快
```shell
#900秒超过1个key被修改
save 900 1
#300秒超过10个key被修改
save 300 10
#60秒超过1万个key被修改
save 60 10000
#RDB文件名
dbfilename dump.rdb
#RDB文件保存路径
dir /opt/redis5/data/6379

```

> 日志: AOF (append only file)
> redis的写操作记录到文件中
> 优点: 
> 1. 丢失数据少
> 2. redis中, RDB和AOF可以同时开启(但是4.0之前, 开启了AOF只会用AOF恢复; 4.0之后, AOF会先将上个时点的RDB恢复, 再进行AOF增量日志记录的恢复)
> 重写命令: bgrewriteaof
> 弊端:
> 1. 体量无限变大(4.0以前: 将一段时间内的日志重写(删除抵消命令, 合并重复命令), 4.0以后:将老的数据RDB到aof文件中, 再将增量的操作以指令的方式Append到AOF(RDB+AOF的混合体文件))
> 2. 恢复慢
> 3. 写日志操作会触发系统IO, 影响redis服务的读写性能(appendfsync always, appendfsync everysec, appendfsync no 三种策略, 默认每秒写, 性能与持久性的折中)
> no-appendfsync-on-rewrite no 这项配置no表示如果在RDB, 则redis不主动调用对磁盘刷写(不去争抢IO, 但容易丢数据), 配置yes则数据安全性高
> aof-use-rdb-preamble yes 在redis4.0之后才有的rdb+aof的重写功能
> auto-aof-rewrite-percentage 100, auto-aof-rewrite-min-size 64mb 表示每增长64mb(乘以百分比)就会触发一次重写, 并记录重写后的大小开始记录增长大小
