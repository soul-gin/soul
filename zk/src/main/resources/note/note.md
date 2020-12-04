#### zookeeper 分布式协调服务
##### 主从集群模型(数据存放内存/读写快)
> 运行状态:(不可用状态恢复到可用状态应该越快越好/200ms)
> 1. 可用状态(leader存活)
> 2. 不可用状态(leader宕机)

> 存在问题:
> 1. leader宕机可能性
> 2. 不可靠的集群

> zk处理:
> 不可用至可用状态恢复速度快, 以保障集群的高可用性(200ms)
> 一个3节点zk集群可以支撑[8万并发读 或 2万并发写](https://zookeeper.apache.org/doc/current/zookeeperOver.html), 也说明一点, zk适合大量并发读取, 不适合频繁去修改

##### 存储格式:
> 持久节点
> 临时节点(session): 客户端连接zk, 会创建一个session, 客户端宕机, 则session会被删除; 连接断开时, 客户端会删除对应session; 所以临时节点可以天然作为分布式锁(不需要过期时间, 操作完成或者客户端挂掉均可将session删除, 即锁被释放; 因不需要过期时间, 那么也就不需要锁的续租逻辑)
> 序列节点

##### 特征
> 顺序一致性: 客户端的更新将按发送顺序应用
> 原子性: 更新成功或失败, 没有部分结果
> 统一视图: 无论服务器连接到哪个服务器, 客户都将看到相同的服务视图
> 可靠性: 一旦应用了更新, 它将从那时起持续到客户端覆盖更新
> 及时性: 系统的客户视图保证在特定时间范围内是最新的

**扩展性**
> 框架架构: 角色-> Leader  Follower  Observer
> 读写分离: Leader宕机后, 只有 Follower 才能参与选举(Observer不参与, 主要是放大查询能力, 如要新增observer, 配置文件中配置为:  server.4=kafka03:2888:3888:observer)

**可靠性**
> 对内: 快速恢复leader(200ms)
> 协议的关键点: 1. 过半通过; 2.最大版本号(最新纪元)控制一次逻辑提议(过滤掉并发重复提议)
> paxos(基于消息传递的一致性算法, 前提是可信的通讯环境(拜占庭将军问题))
> 1. 可参与选举的节点(非observer, 议员), 旧主节点宕机, 新主必须获得 ["可选举节点数"/2+1] 票才能正式作为新主节点
> 2. 每个选举节点只会通过大于当前节点记录的版本号的提议, 否则会拒绝; 并且通过后会更新版本号(因每个节点都可以发起选主投票, 避免一次逻辑选举中, 多个节点同时发起并获取同票数 / 保证一轮逻辑选举发起的选举每个节点不重复投票, 仅投一次票)

> zab协议(zk对paxos的精简)
> zk数据状态在内存中, 磁盘持久化日志; 写操作是原子性的(只有成功/失败, 没有中间状态); 写操作通过广播形式(集群消息同步通讯)
> 3节点集群写操作为例:
> 客户端触发写入操作, 连接了一个Follower节点
> 1. client 发送 create /ooxx "some message" 请求到 zk的Follower服务节点
> 2. zk的Follower服务节点向Leader节点发送同步写请求(create /ooxx "some message")
> 3. Leader节点生成事务Zxid:1
> 4. Leader节点向两个Follower节点发送记录写入操作log的消息(Leader维护了和Follower通讯的消息队列, 所以只要Follower最终能消费完Leader的消息, 就能保障最终一致性)
> 5. Follower节点如接受到消息, 则进行写入log操作, 完成则返回ok
> 6. Leader在收到过半ok则发送写入内存操作(这里其实是2PC, 两阶段提交处理分布式事务, 保证原子性)
> 7. Leader在收到内存写操作过半ok, 则返回触发client写操作的Follower一个ok, Follower也返回clinet端ok
> 8. 注意: 在过程中, Follower接受client读请求可能会需要和2888端口同步(sync), 以返回最新结果

> 选举(关联关系: myid, Zxid)
> 新的Leader:
> 1. 经验最丰富/所持有数据最多最新: Zxid最大;  
> 2. 经验相同配置的序号最大(天资): myid最大;

> 第一次启动集群; 重启集群;  Leader挂了之后三种情况
> 假设3节点: node01(myid=1, Zxid=8), node02(myid=2, Zxid=8), node03(myid=3, Zxid=7)
> node03发起投票, 并给自己投一票, 将(myid=3, Zxid=7)消息发送给node01和node02, 假设先到node01, node01发现(myid=3, Zxid=7)比较自己的(myid=1, Zxid=8), Zxid更小, 则拒绝, 并触发自己发起投票(认为对方投票无效, 发起自认为正确的投票), 并给自己投票, 把(myid=1, Zxid=8)发送给node02和node03节点
> 假设消息到了node02, node02把(myid=1, Zxid=8)和自己的(myid=2, Zxid=8)比较后, 拒绝给node01投票, 并自己发起投票, 且给自己投票
> 最终node02会在集群中达到共识3票, node01达到2票, node02会成为新的Leader并开启2888端口
> 

> 对外: 提供数据可靠可用, 保证一致性(最终一致性)


**时序性**

**快速**

##### 配置
```shell
# 集群中主从心跳的时间间隔
tickTime=2000
# 可以容忍多少次心跳检测失败(结合tickTime, 目前是2000*10, 即20秒内)
initLimit=10
# 可以容忍多少次同步数据失败(结合tickTime, 目前是2000*5, 即10秒内)
syncLimit=5
# 持久化数据的存储目录
dataDir=/home/zookeeper/zookeeper-3.4.6/data
# the port at which the clients will connect
clientPort=2181
# 允许客户端最大连接数
#maxClientCnxns=60
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1

#集群信息3台(主从节点, 3888集群选主投票端口, 2888和主通讯端口)
server.1=kafka01:2888:3888
server.2=kafka02:2888:3888
server.3=kafka03:2888:3888

```

##### zk端口
> 3888 选主投票用
> 2888 leader接受write请求
```shell
#安装 netstat 命令
yum install -y net-tools
#查看2888 和 3888端口
netstat -natp | egrep '(2888|3888)'
#显示为
#监听了本机放开的3888端口
#tcp6       0      0 192.168.25.106:3888     :::*                    LISTEN      2750/java
#本机的3888端口被107节点连接了
#tcp6       0      0 192.168.25.106:3888     192.168.25.107:46733    ESTABLISHED 2750/java
#本机使用57376(随机生成端口号)去连接了108节点的2888端口(108节点目前为leader)
#tcp6       0      0 192.168.25.106:57376    192.168.25.108:2888     ESTABLISHED 2750/java
#本机的3888端口被108节点连接了
#tcp6       0      0 192.168.25.106:3888     192.168.25.108:45315    ESTABLISHED 2750/java

```

##### 命令使用
```shell
[root@gin ~]# zkCli.sh
[zk: localhost:2181(CONNECTED) 0] ls /
[zookeeper]
# 查看包含的命令
[zk: localhost:2181(CONNECTED) 1] help
ZooKeeper -server host:port cmd args
        stat path [watch]
        set path data [version]
        ls path [watch]
        delquota [-n|-b] path
        ls2 path [watch]
        setAcl path acl
        setquota -n|-b val path
        history
        redo cmdno
        printwatches on|off
        delete path [version]
        sync path
        listquota path
        rmr path
        get path [watch]
        create [-s] [-e] path data acl
        addauth scheme auth
        quit
        getAcl path
        close
        connect host:port

#创建节点(注: 不写入数据节点无法创建成功)
[zk: localhost:2181(CONNECTED) 2] create /ooxx ""
Created /ooxx
[zk: localhost:2181(CONNECTED) 3] ls /
[zookeeper, ooxx]
#在创建的节点下面新增节点
[zk: localhost:2181(CONNECTED) 4] create /ooxx/xxoo ""
Created /ooxx/xxoo
[zk: localhost:2181(CONNECTED) 5] ls /ooxx/xxoo
[]
#获取节点下的数据
[zk: localhost:2181(CONNECTED) 6] get /ooxx
#创建时写入的数据(注: 不写入数据节点无法创建成功)
""
#当前节点的ID(c表示创建时的Zxid), ID的序列号由主节点维护, 所以可以单调递增
cZxid = 0x600000030
ctime = Wed Dec 02 17:59:09 CST 2020
#当前修改后(如有变化)节点的ID(m表示修改时的Zxid); 0x6表示集群的leader已经经过5次选举, 达到第6代(纪元)
mZxid = 0x600000030
mtime = Wed Dec 02 17:59:09 CST 2020
#最后子(person)节点的ID(p表示当前节点下最后的节点号时的Zxid)
pZxid = 0x60000012b
cversion = 1
dataVersion = 0
aclVersion = 0
#临时节点持有者, 0x0表示没有归属者
ephemeralOwner = 0x0
dataLength = 2
numChildren = 1
[zk: localhost:2181(CONNECTED) 7] get /ooxx/xxoo
""
cZxid = 0x60000012b
ctime = Wed Dec 02 18:02:08 CST 2020
mZxid = 0x60000012b
mtime = Wed Dec 02 18:02:08 CST 2020
pZxid = 0x60000012b
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0

#测试最终子节点pZxid
[zk: localhost:2181(CONNECTED) 12] get /ooxx
""
cZxid = 0x600000030
ctime = Wed Dec 02 17:59:09 CST 2020
mZxid = 0x600000030
mtime = Wed Dec 02 17:59:09 CST 2020
#最终子节点pZxid变成新创建的 /ooxx/gin 的创建节点id
pZxid = 0x700000003
cversion = 2
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 2
numChildren = 2
[zk: localhost:2181(CONNECTED) 13] get /ooxx/gin
""
cZxid = 0x700000003
ctime = Thu Dec 03 11:21:59 CST 2020
mZxid = 0x700000003
mtime = Thu Dec 03 11:21:59 CST 2020
pZxid = 0x700000003
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0



```


##### 创建临时节点
```shell
#创建临时节点
[zk: localhost:2181(CONNECTED) 14] create -e /ephemeralNode ""
Created /ephemeralNode
#查看(当前客户端及其他客户端均可查看到, 临时节点session进行了共享)
#注意, 如果客户端连接的server宕机, 对应session依然会存在(session创建和删除和Zxid关联起来了, 每次创建或删除均会消耗Zxid序列号), 因为session共享了, 可以从其他节点查询到
[zk: localhost:2181(CONNECTED) 15] get /ephemeralNode
""
cZxid = 0x700000004
ctime = Thu Dec 03 11:36:56 CST 2020
mZxid = 0x700000004
mtime = Thu Dec 03 11:36:56 CST 2020
pZxid = 0x700000004
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x176268ea3260000
dataLength = 2
numChildren = 0
#ctrl+c 退出创建临时节点的客户端, 从其他客户端发现对应临时节点被删除了
[zk: localhost:2181(CONNECTED) 0] ls /
[zookeeper, ooxx]


```

##### 序列节点
```shell
#可以避免分布式情况下创建相同的目录
#使用场景: 分布式自增序列号(数据隔离, 分布式命名)
[zk: localhost:2181(CONNECTED) 0] ls /ooxx
[xxoo, gin]
[zk: localhost:2181(CONNECTED) 1] create -s /ooxx/seq ""
Created /ooxx/seq0000000002
[zk: localhost:2181(CONNECTED) 2] create -s /ooxx/seq ""
Created /ooxx/seq0000000003
[zk: localhost:2181(CONNECTED) 3] create -s /ooxx/seq ""
Created /ooxx/seq0000000004
[zk: localhost:2181(CONNECTED) 4] create -s /ooxx/seq ""
Created /ooxx/seq0000000005
[zk: localhost:2181(CONNECTED) 5] ls /ooxx
[xxoo, seq0000000005, seq0000000003, gin, seq0000000004, seq0000000002]


```

##### 顺序临时节点
```shell
[zk: localhost:2181(CONNECTED) 10] create /seNode ""
Created /seNode
[zk: localhost:2181(CONNECTED) 11] create -s -e /seNode/seq ""
Created /seNode/seq0000000000
[zk: localhost:2181(CONNECTED) 13] ls /seNode
[seq0000000000]

```

##### watch 监控
> 通过zk目录树结构(统一视图)
> 例: 两client有需求, client间互相检测是否存活
> 方案1: client之间心跳检测, 由client实现
> 方案2: 基于zk的watch功能, client1在zk上创建临时节点 /tmp/client1 , client2可以通过get /tmp/client1 查询到对应节点后, 再通过 watch /tmp/client1 来监控; 同时 /tmp/client1 (会包含事件(event): create delete change children ), 
```shell

```

##### zookeeper应用
> 统一配置管理, 1M数据(节点可存储)
> 分组管理, path结构
> 统一命名, 顺序节点(sequential)
> 同步, 分布式锁
> 1. 普通分布式锁, 直接使用临时节点
> 2. 队列式事务锁, 锁都依托一个父节点且具备 -s , 代表父节点可以有多把顺序排列的锁(后面的锁监听前面锁)
> 3. HA选主, 先抢到锁的作为主(先在zk创建成功节点)


