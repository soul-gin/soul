#### 单机/单节点/单实例存在的问题
> 单点故障
> 容量限制
> 并发压力

#### AKF
>X: 全量, 镜像(节点的备份/主备)
>Y: 按功能/业务拆分数据至多个节点/实例
>Z: 对单个功能/业务, 按时间范围/ID范围(或优先级别等逻辑)进行逻辑拆分

>以一个系统的数据库(鉴权业务/功能1/功能2/报表业务)为例:
>X: 相当于整库的备份(主备)
>Y: 相当于将"鉴权业务/功能1/功能2/报表业务"这些业务包含的表, 从1个库中, 拆分至4个库(分库/垂直拆分)
>Z: 相当于将"鉴权业务"的用户表按ID拆分至多个库(分表/水平拆分)

> 解决一个问题, 必然带来其他问题: AKF理论处理了单点问题, 带来了数据一致性问题
> 强一致性: 通过同步/阻塞方式, 所有节点阻塞直到数据全部都一致, 破坏可用性(CP)
> 弱一致性: CAP, 通过异步方式, 容忍数据丢失一部分(AP)
> 最终一致性: BASE, 同步阻塞数据至某个中间件(可靠, 集群, 响应速度够快, 如: kafka), 使得数据最终通过中间件达到最终一致性(将数据同步问题转嫁三方)(AP, 最终C)

> 一致性( C ) : 在分布式系统中的所有数据备份，值在同一时刻是否相同（不存在绝对一致性， 忽略了网络传输耗时（超过一定距离需独自部署集群））
> 可用性( A ) : 在集群中一部分节点故障后，集群整体是否还能**正确**响应客户端的读写请求（返回报错不是可用）
> 分区容忍性( P ) : 是否容忍多台服务器之间发生网络分区故障（选择P表示发生网络分区依然能**正确**响应请求）， 如果能容忍，意味着发生了分区(脑裂)的情况, 集群保证每台服务器返回的数据一致（如zk： 由1/2以上节点选举出主，主负责修改数据）

#### redis解决单点问题
> 通过主从集群方式
> 主的单点问题, 通过对主做HA(高可用), 自动故障转移, 对于客户端(client), 感知的是一个集群( 一个主节点提供读写服务, 从节点提供读服务, 主节点宕机, 某个从节点可以升级为主节点继续提供读写服务 )

#### 主从复制
> 伪分布式，同服务器上启动三个节点
```shell
#查看是否已启动进程
ps -ef|grep redis
#启动多实例, port分别为6379 6380 6381
cd /opt/redis5/bin
./install_server.sh
#拷贝配置文件
cd ~/
mkdir redis
cd redis
ln /etc/redis/6379.conf 6379.conf
ln /etc/redis/6380.conf 6380.conf
ln /etc/redis/6381.conf 6381.conf
ln /var/log/redis_6379.log redis_6379.log
ln /var/log/redis_6380.log redis_6380.log
ln /var/log/redis_6381.log redis_6381.log
#查看进程, 关闭redis
ps -ef|grep redis
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9
#重启redis
redis-server ./6379.conf
redis-server ./6380.conf
redis-server ./6381.conf
```

> 连接客户端端进行测试
```shell
#多个shell终端
redis-cli -p 6379
redis-cli -p 6380
redis-cli -p 6381

#在6380客户端输入追随6379
replicaof 127.0.0.1 6379
```

> 在6380的日志中可以看到如下信息（主要包含：连接检查，清除当前节点数据，开启同步）
> 6188:S 31 Aug 2020 22:10:31.384 * Connecting to MASTER 127.0.0.1:6379
6188:S 31 Aug 2020 22:10:31.384 * MASTER <-> REPLICA sync started
6188:S 31 Aug 2020 22:10:31.384 * Non blocking connect for SYNC fired the event.
6188:S 31 Aug 2020 22:10:31.384 * Master replied to PING, replication can continue...
6188:S 31 Aug 2020 22:10:31.385 * Trying a partial resynchronization (request 5425318284dba4cda94764740be05f7de4911a56:1).
6188:S 31 Aug 2020 22:10:31.579 * Full resync from master: d4ed2d7b332d97bd673ae353ce87f78fbce1b65a:0
6188:S 31 Aug 2020 22:10:31.579 * Discarding previously cached master state.
6188:S 31 Aug 2020 22:10:31.579 * MASTER <-> REPLICA sync: receiving 176 bytes from master
6188:S 31 Aug 2020 22:10:31.580 * MASTER <-> REPLICA sync: Flushing old data
6188:S 31 Aug 2020 22:10:31.580 * MASTER <-> REPLICA sync: Loading DB in memory
6188:S 31 Aug 2020 22:10:31.581 * MASTER <-> REPLICA sync: Finished with success

> 同步测试
```shell
#在6379里插入数据
set k1 123
#在6380中查询
get k1
#在6380中写入，发现报错（不允许写）
#(error) READONLY You can'a04ThreadConcurrent write against a read only replica.
set k2 456
```

> 重启测试
```shell
#启动6381
redis-server ./6381.conf --replicaof 127.0.0.1 6379
#6379插入数据
set k2 456
#停止6381
ps -ef|grep 127.0.0.1:6381 | grep -v grep | awk '{print $2}' | xargs kill -9
#6379插入数据
set k3 789
set k4 abc
#重启6381
redis-server ./6381.conf --replicaof 127.0.0.1 6379
redis-cli -p 6381
127.0.0.1:6381> keys *
1) "k2"
2) "k1"
3) "k3"
4) "k4"
```

> 主节点宕机
```shell
#停掉6379
ps -ef|grep 127.0.0.1:6379 | grep -v grep | awk '{print $2}' | xargs kill -9
#发现从节点只还能读取，不能写入， 且日志报错提示
#6188:S 31 Aug 2020 22:32:49.008 * MASTER <-> REPLICA sync started
#6188:S 31 Aug 2020 22:32:49.008 # Error condition on socket for SYNC: Connection refused
#6188:S 31 Aug 2020 22:32:50.015 * Connecting to MASTER 127.0.0.1:6379
#6380停止追随
redis-cli -p 6380
replicaof no one
#日志停止报错，且可读写

```


> 主从配置
```shell
#从机在同步时，是否允许从节点被查询（主数据传输过来后，flushdb之前）
replica-serve-stale-data yes
#从节点是否只读
replica-read-only yes
#直接通过网络传输rdb
repl-diskless-sync no
#增量复制的队列大小（与增删改操作数量正相关，可确定宕机后数据恢复速度）
repl-backlog-size 1mb
#最少几次/几秒写成功（重试次数/秒）
min-replicas-to-write 3
min-replicas-max-lag 10
```


#### redis高可用（HA），哨兵（sentinel）
> 哨兵先连接主节点，通过主节点的发布订阅功能（psubscribe * 命令查看到有__sentinel__的通道），获取到从节点的信息，再对集群进行监控
```shell
# 关闭所有节点
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9
# 新增3个监控配置文件
cd ~/redis/
#监控6379
cat >26379.conf<<EOF
#sentinel端口号
port 26379
#监听逻辑mymaster集群，对应ip，port，投票权重值
sentinel monitor mymaster 127.0.0.1 6379 2
EOF

#监控6380
cat >26380.conf<<EOF
#sentinel端口号
port 26380
#监听逻辑mymaster集群，对应ip，port，投票权重值
sentinel monitor mymaster 127.0.0.1 6379 2
EOF

#监控6381
cat >26381.conf<<EOF
#sentinel端口号
port 26381
#监听逻辑mymaster集群，对应ip，port，投票权重值
#一套sentinel可以监控多套redis集群
sentinel monitor mymaster 127.0.0.1 6379 2
EOF

```

> 启动实例
```shell
ps -ef|grep redis
# 启动主从节点
redis-server ./6379.conf
redis-server ./6380.conf --replicaof 127.0.0.1 6379
redis-server ./6381.conf --replicaof 127.0.0.1 6379

# 启动哨兵监控（redis-server作为sentinel启动，而不是键值对数据库）
redis-server ./26379.conf --sentinel
#对应日志显示：启动了sentinel，发现主节点有两个从节点6380和6381
#38074:X 31 Aug 2020 23:27:32.542 # +monitor master mymaster 127.0.0.1 6379 quorum 2
#38074:X 31 Aug 2020 23:27:32.543 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6379
#38074:X 31 Aug 2020 23:27:32.557 * +slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379

#在分别启动另外两个监控节点
redis-server ./26380.conf --sentinel
redis-server ./26381.conf --sentinel

#停止主节点
ps -ef|grep 127.0.0.1:6379 | grep -v grep | awk '{print $2}' | xargs kill -9
ps -ef|grep redis

#发现其中一台监控记录了选举过程
#主节点宕机，需要两票选出新节点
39191:X 31 Aug 2020 23:32:09.592 # +sdown master mymaster 127.0.0.1 6379
39191:X 31 Aug 2020 23:32:09.682 # +odown master mymaster 127.0.0.1 6379 #quorum 2/2
#选举出6381作为新节点
39191:X 31 Aug 2020 23:32:09.831 # +selected-slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
#把6381设置为no one， 代替人为操作
39191:X 31 Aug 2020 23:32:09.831 * +failover-state-send-slaveof-noone slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
#将监控master从6379切换为6381
39191:X 31 Aug 2020 23:32:11.995 # +switch-master mymaster 127.0.0.1 6379 127.0.0.1 6381
#将6380设置为6381的从节点
39191:X 31 Aug 2020 23:32:11.995 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
#重新分配集群信息，后续主上线也是6381的从节点
39191:X 31 Aug 2020 23:32:11.995 # +switch-master mymaster 127.0.0.1 6379 127.0.0.1 6381
39191:X 31 Aug 2020 23:32:11.995 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
39191:X 31 Aug 2020 23:32:11.995 * +slave slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6381
39191:X 31 Aug 2020 23:32:42.029 # +sdown slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6381

```

> 测试
```shell
redis-cli -p 6381
127.0.0.1:6381> set k1 999
OK

redis-cli -p 6380
127.0.0.1:6380> get k1
"999"

#重启redis, 发现6379作为从节点启动了
cd /root/redis
redis-server ./6379.conf
[root@gin ~]# redis-cli -p 6379
127.0.0.1:6379> set k1 1
(error) READONLY You can'a04ThreadConcurrent write against a read only replica.
127.0.0.1:6379> get k1
"999"

# 关闭所有节点
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9


```

#### 容量问题
> 方案：

> 1.按业务划分：不同业务不同redis实例

> 2.业务无法拆分情况下：


> modula（类似sharding/分片），进行算法上的区分：对key进行hash取模（模以redis节点/node数）方式找到对应的redis实例；这种方式对分布式节点扩展不友好（node增加，绝大多数key需要重新分配（旧节点数和新节点数的公倍数可以落在原位置））

> random（消息队列方式），多个redis， 每个redis都存有list类型的key，例topic01，客户端随机lpush 随机写入任意（random）redis中对应topic01中，消费端通过rpop随机消费任意redis中key=topic01中的数据（key类似kafka中的topic名，每个redis就类似kafka的分区），优势就是redis基于内存，kafka基于磁盘

> kemata（一致性哈希），hash（crc16 crc32 fnv md5）映射算法，不用key模node数，而是对key和node（redis节点数）同时计算得到一个整数值，并规划成一个环形（哈希环）
> 例: 3个node的redis, 环是0-2^32范围 , 假设算出node1=1431655765， node2=2863311530，node3=4294967296（或0），如果k1=1431655761，那么存放在比它更大的node1节点上；如果k2=1431655769，那么存放在比它更大的node2节点上；
> 即node计算的值管范围，key计算的值为点，点去找所在范围内的node即可，那么新增节点只会部分范围的数据重新分配

> 优点: 增加节点可以分担其他节点的压力, 且不像直接对节点数取模的算法造成全局洗牌的情形
> 缺点: 新增节点会造成一小部分数据不能命中; 击穿, 压力到了mysql数据库
> 解决方案1: 设法去取距离key最近的2个物理节点
> 解决方案2: 重新缓存, 旧key通过LRU, LFU淘汰

> 后续映射优化，新增虚拟节点，每个实际节点管理部分虚拟节点(如将一个node的ip后面拼接0-9这10个数字, 生成10个虚拟节点)，每个虚拟节点管理一定的实际数字范围，以降低可能发生的数据倾斜问题

> redis集群处理方式(http://www.redis.cn/topics/cluster-tutorial.html):
> redis有16384个槽位, ,每个key通过CRC16校验后对16384取模来决定放置哪个槽.集群的每个节点负责一部分hash槽,举个例子,比如当前集群有3个节点,那么:
> 节点 A 包含 0 到 5500号哈希槽.
> 节点 B 包含5501 到 11000 号哈希槽.
> 节点 C 包含11001 到 16384号哈希槽.
> 这种结构很容易添加或者删除节点. 比如如果我想新添加个节点D, 我需要从节点 A, B, C中得部分槽到D上.(槽的移动同时是数据的移动) 如果我想移除节点A,需要将A中的槽移到B和C节点上,然后将没有任何槽的A节点从集群中移除即可. 由于从一个节点将哈希槽移动到另一个节点并不会停止服务,所以无论添加删除或者改变某个节点的哈希槽的数量都不会造成集群不可用的状态.

>优点:
>客户端无感, 服务端集群中, 每台服务器知道集群中的节点信息, 并且知道默认的每个节点分配了哪些槽位(同一套算法); 客户端只需要访问其中一台redis, 即会返回客户端需要访问哪台节点, 再去对应节点操作即可
>缺点:
>需要进行聚合操作或事务的key分配在多个节点时, 无法进行对应操作
>解决方案: hash tag
>插入的key为: {my_hash_tag}key1 {my_hash_tag}key2
>用相同的hash tag进行槽位计算

#### twemproxy redis代理服务(https://github.com/twitter/twemproxy.git)
>redis服务端代理
> 在线安装方式: 
```shell
# 时间同步, 避免make报错(make: Warning: File `Makefile.am’ has modification time 5691744 s in the future)
yum install -y ntp
#使用阿里云服务同步
ntpdate ntp1.aliyun.com
# 查看当前时间
date +"%Y-%t03M-%d %H-%M-%S"

# 直接在github上下载zip包
# 或者
wget https://github.com/twitter/twemproxy/archive/master.zip
unzip master.zip
mv twemproxy-master twemproxy
cd twemproxy
#配置为高版本的仓库, 可查看 https://developer.aliyun.com/mirror/epel?spm=a2c6h.13651102.0.0.3e221b11ibODVV 
wget -O /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-7.repo
yum install -y automake libtool
# 主要针对autoreconf的版本, 如有报错 Autoconf version 2.XX or higher is required 可以通过 autoreconf --version 来确认(可根据后续离线安装方式切换版本)
# 生成 configure 可执行文件
autoreconf -fvi
# 执行
./configure
# 编译
make
# 切换至编译后目录, 将启动脚本放至 /etc/init.d/ 下, 方便启动
cd scripts
cp nutcracker.init /etc/init.d/twemproxy
cd /etc/init.d/
chmod +x twemproxy
mkdir /etc/nutcracker/ 
cp ~/twemproxy/conf/* /etc/nutcracker/
cp ~/twemproxy/src/nutcracker /usr/bin/
cd /etc/nutcracker/

#保留 alpha 节点即可, 其他可以删除
#对应 alpha 的servers节点
  servers:
   - 127.0.0.1:6379:1
#需要修改成(对应集群3个节点)
  servers:
   - 127.0.0.1:6382:1
   - 127.0.0.1:6383:1

# 关闭所有节点
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9
#确保对应servers中配置的redis节点启动后(目前是集群, 非哨兵sentinel高可用模式)
# 启动多个redis实例 6382 6383
install_server.sh 
#启动redis代理
service twemproxy start
#连接代理服务器
redis-cli -p 22121
127.0.0.1:22121> set k1 1
OK
127.0.0.1:22121> set k2 2
OK
127.0.0.1:22121> set k3 3
OK
#查看key分布
[root@gin ~]# redis-cli -p 6382
127.0.0.1:6382> keys *
(empty list or set)
[root@kafka01 ~]# redis-cli -p 6383
127.0.0.1:6383> keys *
1) "k2"
2) "k3"
3) "k1"
#发现根据kemata(一致性哈希)算法, key均分布在了6383节点
#注意: twemproxy的劣势: 不支持操作
#成本高, 不支持 key *
127.0.0.1:22121> key *
Error: Server closed the connection
#不支持watch
127.0.0.1:22121> watch k1
Error: Server closed the connection
#不支持事务,数据分治了
127.0.0.1:22121> multi
Error: Server closed the connection



```

> 离线安装方式
```shell
# 避免 autoconf 版本过低
yum remove autoconf  

# 时间同步, 避免make报错
yum install -y ntp
#使用阿里云服务同步
ntpdate ntp1.aliyun.com
# 查看当前时间
date +"%Y-%t03M-%d %H-%M-%S"

# 离线安装方式: 跟进linux版本选择 automake、libtool、autoconf
automake-1.12.1.tar.gz 包下载地址：http://ftp.gnu.org/gnu/automake/
autoconf-2.69.tar.gz 包下载地址：http://ftp.gnu.org/gnu/autoconf
libtool-2.2.4.tar.gz 包下载地址：http://ftp.gnu.org/gnu/libtool/
twemproxy-master.zip 包下载地址：https://github.com/twitter/twemproxy/tree/master
下载完成之后，依次解压，并安装
tar -xf autoconf-2.69.tar.gz   
./configure   
make && make install  
  
tar -xf automake-1.12.1.tar.gz   
./configure
make && make install  
  
tar -xf libtool-2.2.4.tar.gz  
./configure   
make && make install 

unzip twemproxy-master.zip   
cd twemproxy-master  
autoreconf -ivf  
./configure   
make
#后续安装及测试参考在线安装即可
```

#### predixy redis代理服务(https://github.com/joyieldInc/predixy.git)
```shell
#编译需要c++11, 建议直接拿releases版本(已经编译好的)
# https://github.com/joyieldInc/predixy/releases
# https://github.com/joyieldInc/predixy/releases/download/1.0.5/predixy-1.0.5-bin-amd64-linux.tar.gz
# 安装
mkdir predixy
cd predixy/
wget https://github.com/joyieldInc/predixy/releases/download/1.0.5/predixy-1.0.5-bin-amd64-linux.tar.gz
tar -zxf predixy-1.0.5-bin-amd64-linux.tar.gz
cd predixy-1.0.5
```

> 测试哨兵 sentinel
```shell
cd conf
vim predixy.conf
# 修改下列配置, 开启哨兵 sentinel.conf 配置, 注释 try.conf 配置
################################### SERVERS ####################################
# Include cluster.conf
Include sentinel.conf
# Include try.conf

#使用块编辑模式
#光标定位到 ## Examples: 的开头位置
#  : 表示开启冒号模式;  . 表示从当前行开始;  ,$ 表示到最后一行;  y 表示复制
#输入 :.,$y
#将光标定位到文件最后一空白行, 按下p, 即完成"## Examples:"的复制

#使用块编辑模式打开注释
#光标定位到复制好的 ## Examples: 的下一行 #SentinelServerPool 开头位置
#  : 表示开启冒号模式;  . 表示从当前行开始;  ,$ 表示到最后一行;  s 表示查找替换;  /  为sed的分割符;  /#//  表示将#替换为空
#输入 :.,$s/#//

#修改下面配置
    Sentinels {
        + 10.2.2.2:7500
        + 10.2.2.3:7500
        + 10.2.2.4:7500
    }
#替换为
    Sentinels {
        + 127.0.0.1:26379
        + 127.0.0.1:26380
        + 127.0.0.1:26381
    }    
#注意对应配置名为哨兵的集群名称shard001 和 shard002
    Group shard001 {
    }
    Group shard002 {
    }

#启动哨兵集群
cd ~/redis

#修改配置为监控 shard001 shard002 这两个集群
cat >26379.conf<<EOF
#sentinel端口号
port 26379
#监听逻辑redis sentinel集群，对应ip，port，投票权重值
sentinel monitor shard001 127.0.0.1 36379 2
sentinel monitor shard002 127.0.0.1 46379 2
EOF

cat >26380.conf<<EOF
#sentinel端口号
port 26380
#监听逻辑redis sentinel集群，对应ip，port，投票权重值
sentinel monitor shard001 127.0.0.1 36379 2
sentinel monitor shard002 127.0.0.1 46379 2
EOF

cat >26381.conf<<EOF
#sentinel端口号
port 26381
#监听逻辑redis sentinel集群，对应ip，port，投票权重值
sentinel monitor shard001 127.0.0.1 36379 2
sentinel monitor shard002 127.0.0.1 46379 2
EOF


#同步修改 26380.conf 26381.conf 两个配置文件

# 关闭所有节点
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9

# 启动哨兵(sentinel)集群
redis-server 26379.conf --sentinel

redis-server 26380.conf --sentinel

redis-server 26381.conf --sentinel

# 启动主从复制集群
# 集群shard001 
mkdir 36379 36380 46379 46380
cd 36379 
redis-server --port 36379
cd 36380
redis-server --port 36380 --replicaof 127.0.0.1 36379

# 集群shard002 
cd 46379
redis-server --port 46379
cd 46380
redis-server --port 46380 --replicaof 127.0.0.1 46379

# 启动 predixy
cd /root/predixy/predixy-1.0.5/bin
./predixy ../conf/predixy.conf

#测试
[root@gin ~]# redis-cli -p 7617
127.0.0.1:7617> set k1 1
OK
127.0.0.1:7617> get k1
"1"
127.0.0.1:7617> set k2 2
OK
127.0.0.1:7617> get k2
"2"
127.0.0.1:7617> set k3 3
OK
127.0.0.1:7617> set k4 4
OK

#查看36379 key分布
[root@gin ~]# redis-cli -p 36379
127.0.0.1:36379> keys *
1) "k3"
2) "k1"

#查看46379 key分布
[root@gin ~]# redis-cli -p 46379
127.0.0.1:46379> keys *
1) "k2"
2) "k4"

#查看事务支持
[root@gin ~]# redis-cli -p 7617
127.0.0.1:7617> set {ox}k1 1
OK
127.0.0.1:7617> set {ox}k2 2
OK
127.0.0.1:7617> watch {ox}k1
(error) ERR forbid transaction in current server pool

#发现还是报错, 主要是目前监控了两个主从复制集群, 而predixy仅支持一个哨兵模式下的事务
#停止 predixy , 修改配置
ps -ef | grep "predixy" | grep -v grep | awk '{print $2}' | xargs kill -9
#注释掉Group shard002 {}
cd /root/predixy/predixy-1.0.5/conf
vim sentinel.conf
#重启 predixy
cd ../bin/
./predixy ../conf/predixy.conf

#数据只往 shard001 哨兵集群中写入, 所以就支持watch及multi操作了, 不存在多个集群(相当于降级了)
[root@gin ~]# redis-cli -p 7617
127.0.0.1:7617> get k1
"1"
127.0.0.1:7617> set k1 1
OK
127.0.0.1:7617> watch k1
OK
127.0.0.1:7617> multi
OK
127.0.0.1:7617> get k1
QUEUED
127.0.0.1:7617> set k2 22
QUEUED
127.0.0.1:7617> exec
1) "1"
2) OK

```

#### redis官方cluster代理服务(非三方, 老版本(4.x之前)需要rube)
```shell
# 关闭所有节点
ps -ef | grep "redis" | grep -v grep | awk '{print $2}' | xargs kill -9

# 切换到redis安装目录中的 utils/create-cluster
cd /home/redis/redis-5.0.10/utils/create-cluster
# 修改集群实例总数量, 从节点数量
vim create-cluster
# 对应配置, NODES=6, 实例总数为6, REPLICAS=1, 每个主节点1个从, 那么就是3主3从集群
NODES=6
REPLICAS=1
# 创建实例
./create-cluster start

# 实例分配槽位, 分配从节点方案
./create-cluster create
#Master[0] -> Slots 0 - 5460
#Master[1] -> Slots 5461 - 10922
#Master[2] -> Slots 10923 - 16383
#Adding replica 127.0.0.1:30005 to 127.0.0.1:30001
#Adding replica 127.0.0.1:30006 to 127.0.0.1:30002
#Adding replica 127.0.0.1:30004 to 127.0.0.1:30003

# 输入 yes 同意分配方案

# 测试集群
# 通过普通客户端连接方式测试
[root@gin create-cluster]# redis-cli -p 30001
127.0.0.1:30001> set k1 1
# 客户端发现需要跳转到集群中 30003 节点, 普通连接模式无法跳转, 故报错
(error) MOVED 12706 127.0.0.1:30003
127.0.0.1:30001>

# 通过集群客户端连接方式测试
[root@kafka01 create-cluster]# redis-cli -c -p 30001
127.0.0.1:30001> set k1 1
# 触发跳转, 告知客户端先跳至 30003 节点, 然后进行存储
-> Redirected to slot [12706] located at 127.0.0.1:30003
OK
127.0.0.1:30003> get k1
"1"
# 触发跳转, 告知客户端先跳至 30001 节点, 然后进行存储
127.0.0.1:30003> set k2 2
-> Redirected to slot [449] located at 127.0.0.1:30001
OK
127.0.0.1:30001> get k2
"2"
# 触发跳转, 告知客户端先跳至 30003 节点, 然后进行读取
127.0.0.1:30001> get k1
-> Redirected to slot [12706] located at 127.0.0.1:30003
"1"
127.0.0.1:30003> watch k2
-> Redirected to slot [449] located at 127.0.0.1:30001
OK
# 在 30001 节点上开启事务
127.0.0.1:30001> multi
OK
# 事务中的key涉及了 30003 节点
127.0.0.1:30001> set k1 111
-> Redirected to slot [12706] located at 127.0.0.1:30003
OK
127.0.0.1:30003> set k3 3
-> Redirected to slot [4576] located at 127.0.0.1:30001
OK
# 提交时会发现执行失败, 因为中途涉及的key及事务不在同一个节点(中途有触发跳转)
127.0.0.1:30001> exec
(error) ERR EXEC without MULTI
# key和事务在同一节点操作
127.0.0.1:30001> multi
OK
127.0.0.1:30001> set k2 222
QUEUED
127.0.0.1:30001> exec
1) OK
# 使用 hash tag 处理, 避免跳转
127.0.0.1:30001> set {ox}k1 1
-> Redirected to slot [9355] located at 127.0.0.1:30002
OK
127.0.0.1:30002> set {ox}k2 2
OK
127.0.0.1:30002> set {ox}k3 3
OK
127.0.0.1:30002> watch {ox}k1
OK
127.0.0.1:30002> multi
OK
127.0.0.1:30002> set {ox}k2 222
QUEUED
127.0.0.1:30002> get {ox}k3
QUEUED
127.0.0.1:30002> exec
1) OK
2) "3"
# 模拟监听失败, k1在事务中被篡改
127.0.0.1:30002> watch {ox}k1
OK
127.0.0.1:30002> get {ox}k1
"1"
127.0.0.1:30002> multi
OK
127.0.0.1:30002> set {ox}k1 111
QUEUED
127.0.0.1:30002> set {ox}k2 789
QUEUED
# 事务执行过程, 监听锁的值被篡改, 事务失败
127.0.0.1:30002> exec
(nil)

# 停止集群
./create-cluster stop

# 查看集群遗留文件
ll /home/redis/redis-5.0.10/utils/create-cluster

# 清理集群, 并查看
./create-cluster clean && ll

```

> 通过 redis-cli 进行创建(多服务器使用)
```shell
cd /home/redis/redis-5.0.10/utils/create-cluster
# 省去 redis-server 启动实例操作(实际各台服务器自行启动, 配置中添加 cluster-enabled yes 表示是以集群模式启动单台实例)
./create-cluster start
# 使用 redis-cli 创建(指定节点ip port; 指定副本数)
redis-cli --cluster create 127.0.0.1:30001 127.0.0.1:30002 127.0.0.1:30003 127.0.0.1:30004 127.0.0.1:30005 127.0.0.1:30006 --cluster-replicas 1
# 输入 yes 确认分配方案

# 连接测试
[root@gin create-cluster]# redis-cli -c -p 30001
127.0.0.1:30003> set {ox}k1 1
-> Redirected to slot [9355] located at 127.0.0.1:30002
OK
127.0.0.1:30002> set {ox}k2 2
OK
127.0.0.1:30002> watch {ox}k1
OK
127.0.0.1:30002> get {ox}k1
"1"
127.0.0.1:30002> multi
OK
127.0.0.1:30002> set {ox}k2 222
QUEUED
127.0.0.1:30002> exec
1) OK


```

> 数据倾斜处理
```shell
# 执行槽位移动
[root@gin create-cluster]# redis-cli --cluster reshard 127.0.0.1:30001
# redis会显示目前分配情况
#>>> Performing Cluster Check (using node 127.0.0.1:30001)
#M: a6ecd70a85536c04eda09bd263f254160bbd5433 127.0.0.1:30001
#   slots:[0-5460] (5461 slots) master
#   1 additional replica(s)
#S: 2d262072a5e9825a3277b351efe6ee511b05ce94 127.0.0.1:30005
#   slots: (0 slots) slave
#   replicates 13994bcf9e54c876b0a68067289a3475c41fbc90
#M: a01ab710307d3ecdbcfb637dbeb32a2d2b504bab 127.0.0.1:30003
#   slots:[10923-16383] (5461 slots) master
#   1 additional replica(s)
#M: 13994bcf9e54c876b0a68067289a3475c41fbc90 127.0.0.1:30002
#   slots:[5461-10922] (5462 slots) master
#   1 additional replica(s)
#S: b875b872ab2079da35ac21f50a8df62b5ee2ac5c 127.0.0.1:30004
#   slots: (0 slots) slave
#   replicates a6ecd70a85536c04eda09bd263f254160bbd5433
#S: bf64fac4579784f16281ffdea2033953ea04adee 127.0.0.1:30006
#   slots: (0 slots) slave
#   replicates a01ab710307d3ecdbcfb637dbeb32a2d2b504bab

# 要求输入移动多少个槽位到 reshard 指定的节点, 例如输入 2000
How many slots do you want to move (from 1 to 16384)? 2000

# 哪个节点接收移动的槽位, 复制刚显示的需要接收节点的hash id即可( 30003 节点 )
# What is the receiving node ID? a01ab710307d3ecdbcfb637dbeb32a2d2b504bab

# 选择从所有节点还是某些节点移动
# Please enter all the source node IDs.
#  Type 'all' to use all the nodes as source nodes for the hash slots.
#  Type 'done' once you entered all the source nodes IDs.
# 这里选择从第一个节点 30001 移动至
Source node #1: a6ecd70a85536c04eda09bd263f254160bbd5433
Source node #2: done

# 是否确定移动
Do you want to proceed with the proposed reshard plan (yes/no)? yes

# 查看移动信息
[root@kafka01 create-cluster]# redis-cli --cluster info 127.0.0.1:30001
#127.0.0.1:30001 (a6ecd70a...) -> 0 keys | 3461 slots | 1 slaves.
#127.0.0.1:30003 (a01ab710...) -> 0 keys | 7461 slots | 1 slaves.
#127.0.0.1:30002 (13994bcf...) -> 2 keys | 5462 slots | 1 slaves.

# 可以发现 30001 为3461槽位, 移动了2000槽位到 30003节点

```





