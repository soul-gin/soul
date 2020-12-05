
#### list
> 查看help
1. redis-cli --raw  连接成功
2. 127.0.0.1:6379> help @list

> lpush  lpop  rpush  rpop  (l r表示左右反向)
> 同向命令(lpush  lpop): 栈结构
> 反向命令(lpush  rpop): 队列结构
> lrange(这里的l表示为list类型,非方向)
```shell
127.0.0.1:6379> flushdb
OK
127.0.0.1:6379> lpush k1 a b c d e f
6
127.0.0.1:6379> rpush k2 a b c d e f
6
127.0.0.1:6379> lpop k1
f
127.0.0.1:6379> lpop k1
e
127.0.0.1:6379> lpop k1
d
127.0.0.1:6379> rpop k2
f
127.0.0.1:6379> rpop k2
e
127.0.0.1:6379> rpop k2
d
127.0.0.1:6379> rpop k1
a
127.0.0.1:6379> rpop k1
b
127.0.0.1:6379> lpop k2
a
127.0.0.1:6379> lpop k2
b
#其他元素均弹出, 目前剩下c
127.0.0.1:6379> lrange k1 0 -1
c
#类似于在list的头位置进行压栈(push)操作
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> lpush k1 a b c d e f
6
127.0.0.1:6379> lrange k1 0 -1
f
e
d
c
b
a
```

> lindex
```shell
#取出索引下标为1的数据
127.0.0.1:6379> lindex k1 1
e
#取出最后一位
127.0.0.1:6379> lindex k1 -1
a
```

> lset 指定下标进行插入操作: 数组结构
```shell
127.0.0.1:6379> lset k1 3 gin
OK
127.0.0.1:6379> lrange k1 0 -1
f
e
d
gin
b
a

```

> lrem 删除指定个数的元素
```shell
127.0.0.1:6379> lpush k3 1 a 2 a 3 b 4 c 5 a 6 d
12
127.0.0.1:6379> lrange k3 0 -1
d
6
a
5
c
4
b
3
a
2
a
1
#lrem 正数2表示从头开始删除2个 a
127.0.0.1:6379> lrem k3 2 a
2
127.0.0.1:6379> lrange k3 0 -1
d
6
5
c
4
b
3
2
a
1

```

> linsert 指定元素前/后插入一个元素
```shell
127.0.0.1:6379> linsert k3 after 6 a
11
127.0.0.1:6379> linsert k3 before 3 a
12
127.0.0.1:6379> lrange k3 0 -1
d
6
a
5
c
4
b
a
3
2
a
1
127.0.0.1:6379> lrem k3 -2 a
2
127.0.0.1:6379> lrange k3 0 -1
d
6
a
5
c
4
b
3
2
1
```

> blpop 阻塞获取数据: 消息订阅, 单播队列(FIFO)
```shell
#开启3个shell窗口
#前两个窗口输入blpop gin 0, 阻塞获取key=gin的数据, 0表示超时时间不限制
blpop gin 0
#最后一个shell窗口依次执行
127.0.0.1:6379> lpush gin 1
(integer) 1
127.0.0.1:6379> lpush gin 2
(integer) 1
#则第一个等待的shell会先输出
127.0.0.1:6379> blpop gin 0
gin
1
#然后第二个等待的shell再输出
127.0.0.1:6379> blpop gin 0
gin
2
```

> ltrim 头尾指定索引位置以外的数据进行删除
```shell
127.0.0.1:6379> lpush k4 1 22 333 4444 55555 66666 7777777
7
127.0.0.1:6379> lrange k4 0 -1
7777777
66666
55555
4444
333
22
1
127.0.0.1:6379> ltrim k4 0 -1
OK
127.0.0.1:6379> lrange k4 0 -1
7777777
66666
55555
4444
333
22
1
127.0.0.1:6379> ltrim k4 1 -2
OK
127.0.0.1:6379> lrange k4 0 -1
66666
55555
4444
333
22
```

> hash
> 如何存储对象数据
> 通过string方式, 指定分割符, 性能不高(多个key, 遍历查询)
> 通过hash方式
```shell
127.0.0.1:6379> set 007:name gin
OK
127.0.0.1:6379> set 007:age 28
OK
127.0.0.1:6379> get 007:name
gin
127.0.0.1:6379> keys 007*
007:name
007:age

#通过hash数据结构进行存储读取
127.0.0.1:6379> get 007:name
gin
127.0.0.1:6379> hset gin name "gin soul" age 18 address ooxx
3
127.0.0.1:6379> hget gin name
gin soul
127.0.0.1:6379> hget gin address
ooxx
127.0.0.1:6379> hkeys gin
name
age
address
127.0.0.1:6379> hvals gin
gin soul
18
ooxx
127.0.0.1:6379> hgetall gin
name
gin soul
age
18
address
ooxx
127.0.0.1:6379> hincrbyfloat gin age 0.5
18.5
127.0.0.1:6379> hgetall gin
name
gin soul
age
18.5
address
ooxx
```

> set
> 无序去重
> 集合操作(交集 并集 差集)
> 随机事件
```shell
127.0.0.1:6379> flushdb
OK
127.0.0.1:6379> sadd k1 gin nier luffy naruto ooxx xxoo
6
127.0.0.1:6379> smembers k1
naruto
luffy
gin
xxoo
nier
ooxx
127.0.0.1:6379> srem k1 ooxx
1
127.0.0.1:6379> smembers k1
luffy
gin
xxoo
nier
naruto
127.0.0.1:6379> sadd k2 1 2 3 4 5
5
127.0.0.1:6379> sadd k3 4 5 6 7 8
5
127.0.0.1:6379> sinter k2 k3
4
5
127.0.0.1:6379> sinterstore k4 k2 k3
2
127.0.0.1:6379> smembers k4
4
5
127.0.0.1:6379> sunion k2 k3
1
2
3
4
5
6
7
8
127.0.0.1:6379> sdiff k2 k3
1
2
3
127.0.0.1:6379> sdiff k3 k2
6
7
8

```

> srandmember key count
> 正整数: 取出一个去重的结果集(不能超过已有集), 中奖数<人数
> 负数: 取出一个带重复的结果集, 一定满足你需要的数量, 中奖数>人数
> 场景: 抽奖, 抢红包
```shell
127.0.0.1:6379> sadd k5 1 2 3 4 5 6 7
2
127.0.0.1:6379> smembers k5
1
2
3
4
5
6
7
127.0.0.1:6379> srandmember k5 3
6
1
4
127.0.0.1:6379> srandmember k5 3
1
2
4
127.0.0.1:6379> srandmember k5 -3
6
6
3
127.0.0.1:6379> srandmember k5 -10
2
7
6
1
1
6
1
7
2
6
127.0.0.1:6379> srandmember k5 10
1
2
3
4
5
6
7

```

> spop 取出一个随机元素
> 可用于年会抽奖, 抽中的人不会再被抽中
```shell
127.0.0.1:6379> spop k5
3
127.0.0.1:6379> spop k5
7
127.0.0.1:6379> spop k5
4
127.0.0.1:6379> spop k5
6

```

> sorted set 按分数排序, 默认从小分值->大分值(asc)
```shell
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> zadd k1 9 house 1 phone 7 car 99 work
4
127.0.0.1:6379> zrange k1 0 -1
phone
car
house
work
127.0.0.1:6379> zrange k1 0 -1 withscores
phone
1
car
7
house
9
work
99
127.0.0.1:6379> zrange k1 0 1
phone
car
127.0.0.1:6379> zrevrange k1 0 1
work
house
#分值
127.0.0.1:6379> zscore k1 work
99
#排名
127.0.0.1:6379> zrank k1 work
3
#操作分值, 排名会同步变更(场景: 热榜, 排行榜)
127.0.0.1:6379> zincrby k1 100 house
109
127.0.0.1:6379> zrange k1 0 -1 withscores
phone
1
car
7
work
99
house
109

```

> zunionstore 集合操作(默认sum)
```shell
127.0.0.1:6379> flushdb
OK
127.0.0.1:6379> zadd math 80 gin 60 luffy 70 nier
3
127.0.0.1:6379> zadd chinese 60 gin 90 luffy 99 naruto
3
#默认sum, 权重为1
127.0.0.1:6379> zunionstore total 2 math chinese
4
127.0.0.1:6379> zrange total 0 -1 withscores
nier
70
naruto
99
gin
140
luffy
150
#默认sum, 数学权重0.5 语文 1
127.0.0.1:6379> zunionstore total2 2 math chinese weights 0.5 1
4
127.0.0.1:6379> zrange total2 0 -1 withscores
nier
35
naruto
99
gin
100
luffy
120
#使用取最大值操作
127.0.0.1:6379> zunionstore avg 2 math chinese aggregate max
4
127.0.0.1:6379> zrange avg 0 -1 withscores
nier
70
gin
80
luffy
90
naruto
99

```

> sorted set (skip list 跳跃表/类平衡树, 牺牲存储空间提高查询速度)
> 排序如何实现?
> 增删改查的速度?
> 1. 由多层链表构成, 单个元素可包含(前后指针及上下指针)
> 2. 0层元素向上抽取部分代表性分值数据, 作为1层
> 3. 若1层数据量依旧很多, 继续抽取部分数据作为2层
> 4. 插入时, 先看最上层, 比较分值后(比后续元素小或后续无元素了)根据所在区间(上个节点)向下层跳跃, 直到第0层
> 5. 将全链表的比较排序转换成了多次二分查找的结构(牺牲存储空间)
