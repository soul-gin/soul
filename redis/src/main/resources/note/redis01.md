#### why
1. redis(基于内存寻址)对比关系型数据库(基于磁盘IO)寻址速度更快
2. redis(string, list, hashes, set, sorted set)相比memcache(key-value, String), 拥有更多的数据结构, 在插入表现类似(x=1; x=[1,2,3]; x={y=1, z=2}均能转化为k-v), 但是要从value中针对性取出部分数据时, redis的优势就体现出来了(计算向数据转移)
3. redis对比aerospike, redis6版本(c标准库中的并发函数,需要gcc-4.9以上)可以多线程操作数据, 不再存在对多核cpu利用率低的劣势
4. redis6之前, 单进程, 单线程, 单实例(操作数据仅1个线程,会有其他线程处理key过期等操作), 大量客户端连接redis, 先通过与服务器的kernel连接后, 通过epoll(非阻塞多路复用)来获取哪个客户端有数据需要处理

#### install
1. redis-5.0.10
```shell
yum install -y wget
wget http://download.redis.io/releases/redis-5.0.10.tar.gz
yum install -y gcc
```


2. redis-6.0.6
> 下载
```shell
yum install -y wget
wget http://download.redis.io/releases/redis-6.0.6.tar.gz
```

> 升级到gcc4.9
```shell
wget https://copr.fedoraproject.org/coprs/rhscl/devtoolset-3/repo/epel-6/rhscl-devtoolset-3-epel-6.repo -O /etc/yum.repos.d/devtoolset-3.repo
yum -y install devtoolset-3-gcc devtoolset-3-gcc-c++ devtoolset-3-binutils
scl enable devtoolset-3 bash
```
> 或升级更高版本
```shell
#升级到gcc 7.3
#需要注意的是scl命令启用只是临时的,退出shell或重启就会恢复原系统gcc版本
yum -y install centos-release-scl
yum -y install devtoolset-7-gcc devtoolset-7-gcc-c++ devtoolset-7-binutils
scl enable devtoolset-7 bash

#如果要长期使用gcc 7.3的话
echo "source /opt/rh/devtoolset-7/enable" >>/etc/profile
```

3. 安装(出现问题多看解压后的README.md)
```shell
#编译(编译报错后需要清理历史文件: make distclean)
make
#安装(指定程序安装目录, 可以和编译源码包分开)
make install PREFIX=/opt/redis5
```
5. 修改配置文件
```shell
vim /etc/profile
# 添加redis的bin目录作为环境变量
export REDIS_HOME=/opt/redis5
export PATH=$PATH:$REDIS_HOME/bin
# 重新加载配置
source /etc/profile
```
6. 启动
```shell
#将源码包utils目录下的install_server.sh软连接至安装目录
ln -s /home/redis/redis-5.0.10/utils/install_server.sh install_server.sh
#创建配置文件目录
mkdir /opt/redis5/conf
#创建日志目录
mkdir /opt/redis5/log
#创建数据目录
mkdir /opt/redis5/data

#启动
./install_server.sh
#端口6379和redis-server路径可以默认, 其他可以使用上面创建的目录, 或者默认
#install_server.sh可以启动多个redis实例,通过port区分
/opt/redis5/conf/6379.conf
/opt/redis5/log/redis_6379.log
/opt/redis5/data/6379
#确认完配置后,脚本会将redis启动

#查看运行状态
service redis_6379 status
ps -ef|grep redis
#后续启停
service redis_6379 start/status/stop
#或者通过 /etc/init.d/ 目录下脚本启动
/etc/init.d/redis_6379

#连接
./redis-cli

```

