
#### 查看help
1. redis-cli --help
2. redis-cli连接成功, 进入 127.0.0.1:6379> 后, 可以使用 help @set 查看命令帮助; 或输入 help @ 后, 按tab键来查看命令; 或直接输入 help 可以查看help使用方式

#### string
1. 字符
2. 数值
3. bitmap

#### 基本使用
set
```shell
set redis "hello world"
#输出OK
get redis
#输出"hello world"
```

set "key" "value" nx
set "key" "value" xx
```shell
set "redis" "hello world" nx
#输出nil, 表示已存在, 不设置
set "redis2" "hello world" nx
#输出ok, not exists, 可以设置
set "redis2" "hello world2" xx
#输出OK, key存在可以更新
set "redis4" "hello world2" xx
#输出nil, key不存在不可以更新
```

mset  mget 批量插入查询操作
```shell
mset k1 v1 k2 v2 k3 v3
#输出OK
mget k1 k3
#输出
#1) "v1"
#2) "v3"
```

append对value进行追加
```shell
append k1 " append"
get k1
#输出"v1 append"
```

getrange 截取value部分数据
```shell
getrange k1 0 -1
#输出"v1 append"
getrange k1 3 8
#输出"append"
getrange k1 3 -1
#输出"append"
```

strlen 计算长度
```shell
strlen k1
#输出(integer) 9
```

type 查看key的类型
```shell
type k1
#输出string
```

object查看值类型
```shell
object encoding k1
#输出"raw"(包含字符和数字)
set 99 "99"
object encoding 99
#输出"int"
set gin hello
#输出OK
object encoding gin
#输出"embstr"
```

二进制安全(redis传输为字节流(所以使用redis要注意存取的编码); 
字符流(需要通过类型转换))
```shell
#清理数据
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> keys *
(empty list or set)
#虽然有int(数字) embstr(字符串)区分, 但是都只是一个字符
127.0.0.1:6379> set k1 hello
OK
127.0.0.1:6379> set k2 10000
OK
#发现int并不是4个字节表示的数值类型, 表明10000指的5个字符
127.0.0.1:6379> strlen k2
(integer) 5 
```

编解码
```shell
127.0.0.1:6379> set k3 翔
OK
127.0.0.1:6379> get k3
"\xe7\xbf\x94"
127.0.0.1:6379> strlen k3
(integer) 3
#退出当前客户端后, 重新连接(编码取决于当前shell客户端的编码, utf-8对于中文是3字节)
redis-cli --raw
127.0.0.1:6379> strlen k3
3
127.0.0.1:6379> get k3
翔
```

存储都是字节, 计算是redis经过了类型转换
```shell
127.0.0.1:6379> set k4 7
OK
127.0.0.1:6379> incr k4
8
```

msetnx原子操作(批量中一个失败则都失败)
```shell
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> msetnx k1 1 k2 2
1
127.0.0.1:6379> mget k1 k2
1
2
127.0.0.1:6379> msetnx k3 3 k2 2
0
127.0.0.1:6379> mget k1 k2
1
2
```

setbit
```shell
127.0.0.1:6379> flushall
OK
#在二进制下 0000 0000 的第二位设置为1 -> 0100 0000 即ASCII表的第64(16进制: 0x40 )位 @ 符号
127.0.0.1:6379> setbit k1 1 1
0
127.0.0.1:6379> get k1
@
127.0.0.1:6379> strlen k1
1
#在二进制下第8位设置为1, 0100 0000 -> 0100 0001, 即ASCII表的第65位 A 符号
127.0.0.1:6379> setbit k1 7 1
0
127.0.0.1:6379> strlen k1
1
127.0.0.1:6379> get k1
A
#超过8位, 则需要新增一个字节, 0100 0001 -> 
#0100 0001  A
#0100 0000  @
127.0.0.1:6379> setbit k1 9 1
0
127.0.0.1:6379> strlen k1
2
127.0.0.1:6379> get k1
A@
#bitpos查询出现第一个bit值的位置, bitpos key bit [start] [end]
#start表示开始字节的索引(8位一个字节)
#end表示结束字节的索引(8位一个字节)
#表示查询第一次出现1的位置, 范围从0下标字节到0下标字节
127.0.0.1:6379> bitpos k1 1 0 0
1
#表示查询第一次出现1的位置, 范围从1下标字节到1下标字节
127.0.0.1:6379> bitpos k1 1 1 1
9
#表示查询第一次出现1的位置, 范围从0下标字节到1下标字节
127.0.0.1:6379> bitpos k1 1 0 1
1
#表示查询第一次出现0的位置, 范围从0下标字节到1下标字节
127.0.0.1:6379> bitpos k1 0 0 1
0
```

bitcount统计出现bit的个数, bitcount key bit [start] [end]
```shell
127.0.0.1:6379> bitcount k1 0 0
2
127.0.0.1:6379> bitcount k1 0 1
3
```

bitop operation destkey key [key ...]
```shell
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> setbit k1 1 1
0
127.0.0.1:6379> setbit k1 7 1
0
127.0.0.1:6379> get k1
A
127.0.0.1:6379> setbit k2 1 1
0
127.0.0.1:6379> setbit k2 6 1
0
127.0.0.1:6379> get k2
B
#与操作(有0则0), 0100 0001 & 0100 0010 -> 0100 0000
127.0.0.1:6379> bitop and andkey k1 k2
1
127.0.0.1:6379> get andkey
@
#或操作(有1则1), 0100 0001 | 0100 0010 -> 0100 0011
127.0.0.1:6379> bitop or orkey k1 k2
1
127.0.0.1:6379> get orkey
C
```

bitmap应用场景: 统计用户365天内活跃的次数, 窗口期随意
```shell
#sean这个客户在第1 7 364天登录过
127.0.0.1:6379> setbit sean 1 1
0
127.0.0.1:6379> setbit sean 7 1
0
127.0.0.1:6379> setbit sean 364 1
0
#占用字节数
127.0.0.1:6379> strlen sean
46
#最后两天登录了几次
127.0.0.1:6379> bitcount sean -2 -1
1
```

618活动: 送用户礼物, 需要准备多少礼物
僵尸用户
冷热用户/忠诚用户

活跃用户统计, 随机窗口
```shell
#用户编号从0开始递增
#20201101这天1号用户登录了
setbit 20201101 1 1
#20201102这天1, 7号用户登录了
setbit 20201102 1 1
setbit 20201102 7 1
#统计20201101-20201102两天有几个活跃用户
127.0.0.1:6379> bitop or 20201101-2 20201101 20201102
1
127.0.0.1:6379> bitcount 20201101-2 0 -1
2
```

incr decr
场景: 抢购, 秒杀, 详情页, 点赞数, 评论数
规避高并发下,对数据库事务操作, 完全由redis内存操作代替
```shell
127.0.0.1:6379> flushall
OK
127.0.0.1:6379> set k1 9
OK
127.0.0.1:6379> incr k1
10
127.0.0.1:6379> decr k1
9
```








