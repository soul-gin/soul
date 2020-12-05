#### pipeline
> 通过nc模拟客户端请求
```shell
yum install -y nc
#nc连接6379端口, 在阻塞窗口发送命令, 类似redis-cli
nc localhost 6379
flushall
+OK
set k1 6
+OK
incr k1
:7
```

> 通过 echo 及 | 将多条命令一起发送
> pipeline可用于redis数据冷加载
```shell
echo -e "setnx k2 1\n incr k2\n get k2" | nc localhost 6379
:1
:2
$1
2
```

#### 消息订阅
```shell
#开启两个shell窗口
redis-cli
#订阅
subscribe myTopic
Reading messages... (press Ctrl-C to quit)
1) "subscribe"
2) "myTopic"
3) (integer) 1

#发布
redis-cli
127.0.0.1:6379> publish myTopic 1
(integer) 1
127.0.0.1:6379> publish myTopic 2
(integer) 1
127.0.0.1:6379> publish myTopic 3
(integer) 1

#订阅shell接收消息
1) "message"
2) "myTopic"
3) "1"
1) "message"
2) "myTopic"
3) "2"
1) "message"
2) "myTopic"
3) "3"

```

#### redis缓存场景
> 如: 类似微信通讯软件的聊天记录
> 历史数据 -> 数据库存储
> 历史3天内 -> redis中存储, 降低数据库查询压力(滑动时间窗口, 可以使用sorted set, 时间作为分值(定时删除超过3天的))
> 实时性的数据 -> redis发布订阅功能

> 缓存数据 相对数据库不那么重要
> 缓存不是全量数据
> 缓存应该随着访问变化(内存的大小有限, 瓶颈)
> 1. 热数据保留, 淘汰冷数据
> 2. 内存设置: maxmemory <bytes>
> 3. 过期策略: maxmemory-policy noeviction (LFU 使用次数; LRU 距离最近使用间隔时间; 返回报错; 随机丢弃key; 优先回收ttl时间较短的key)
> 缓存应该需要有有效期
>  1. 过期时间不随访问延长
>  2. 重新写入key会剔除过期时间
>  3. 倒计时过期
>  4. 定时过期
>  5. 被动访问时判定过期, 周期轮询判定(牺牲一定内存空间, 换取性能)
```shell
#重新写入key会剔除过期时间
127.0.0.1:6379> flushdb
OK
127.0.0.1:6379> set k1 1
OK
127.0.0.1:6379> ttl k1
(integer) -1
127.0.0.1:6379> expire k1 20
(integer) 1
127.0.0.1:6379> ttl k1
(integer) 18
127.0.0.1:6379> ttl k1
(integer) 17
127.0.0.1:6379> ttl k1
(integer) 16
127.0.0.1:6379> set k1 2
OK
127.0.0.1:6379> ttl k1
(integer) -1

#定时过期
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> set k1 1
OK
127.0.0.1:6379> time
1) "1598865684"
2) "339123"
127.0.0.1:6379> expireat k1 1598866594
(integer) 1
127.0.0.1:6379> ttl k1
(integer) 893
127.0.0.1:6379> ttl k1
(integer) 891
127.0.0.1:6379> set k1 2
OK
127.0.0.1:6379> ttl k1
(integer) -1

```

#### 事务
> 一个客户端的事务不会阻碍其他客户端(哪个客户端的exec命令先到则先执行, 对应客户端的缓存区间的事务相关命令)
> watch 监听一个key(被事务使用的共享资源, cas锁), 如果该key在执行exec前未被其他客户端修改, 则事务正常提交, 否则失败
> multi 开启事务
> exec提交事务
```shell
redis-cli
127.0.0.1:6379> help @transactions
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> multi
OK
127.0.0.1:6379> set k1 1
QUEUED
127.0.0.1:6379> set k2 2
QUEUED
127.0.0.1:6379> exec
1) OK
2) OK

#开启两个shell客户端
#客户端1
127.0.0.1:6379> flushdb
OK
127.0.0.1:6379> set k1 1
OK

#客户端2
127.0.0.1:6379> watch k1
OK
127.0.0.1:6379> multi
OK
127.0.0.1:6379> get k1
QUEUED
127.0.0.1:6379> keys *
QUEUED

#客户端1
127.0.0.1:6379> set k1 7
OK

#客户端2, watch发现k1已经被篡改, 则事务中命令不执行
127.0.0.1:6379> exec
(nil)

```

#### 布隆过滤器
> 缓存穿透: 用户搜索的内容缓存没有, 数据库也没有(黑客利用大量请求进行攻击)
> 优先计算数据库里有哪些数据, 通过映射函数标记到redis的bitmap中
> 这时前端请求到redis, 发现bitmap中标记为没有, 则直接返回
> 映射函数(没有的某些数据也可能通过映射和有的数据为同值, 碰撞)决定了能拦截多少无效请求
> 概率解决问题, 基本能降低99%的无效请求

> 通过应用实现:
> 1.穿透了, 不存在
> 2.将穿透的key数据设置进redis, value标记为不存在
> 3.数据库更新了数据, 需要先删除对应的key
```shell
cd /home/redis/soft
#地址 https://github.com/RedisBloom/RedisBloom
wget https://github.com/RedisBloom/RedisBloom/archive/master.zip
yum install -y unzip
unzip master.zip
cd RedisBloom-master/
#执行编译, 获取生成的redisbloom.so, 将其拷贝至redis所在bin目录同级
make
cp redisbloom.so /opt/redis5/
#重启redis
service redis_6379 status
#使用对应module启动redis(需要绝对路径), 并查看
redis-server --loadmodule /opt/redis5/redisbloom.so
ps -ef|grep redis

redis-cli
127.0.0.1:6379> BF.add k1 1
(integer) 1
127.0.0.1:6379> BF.exists k1 1
(integer) 1
127.0.0.1:6379> BF.exists k1 2
(integer) 0
127.0.0.1:6379> BF.exists k2 1
(integer) 0

```
