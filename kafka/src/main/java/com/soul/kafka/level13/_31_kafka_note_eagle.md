##### 前置条件
- kafka 集群
- mysql
##### 软件信息
- 源码地址
https://github.com/smartloli/kafka-eagle
- 下载地址
http://download.kafka-eagle.org/
- 网盘下载地址
链接：https://pan.baidu.com/s/1b5DjLQBmbz_8Nh7DyLj2BQ 
提取码：gin1 

##### install
- 当前安装演示版本
kafka-eagle-bin-1.4.0.tar.gz

- install
```shell
# 解压
tar -zxf kafka-eagle-bin-1.4.0.tar.gz
# 找到实际安装文件
cd kafka-eagle-bin-1.4.0
# 解压
tar -zxf kafka-eagle-web-1.4.0-bin.tar.gz -C /home/kafka/
# 查看
cd /home/kafka/
ll kafka-eagle-web-1.4.0
```

- 系统变量配置
```shell
# 配置系统环境变量
vim /etc/profile
# 或者
# 配置用户环境变量
vim ~/.bashrc
```
```shell
# 例: 添加如下信息至 /etc/profile 中, 主要是 KE_HOME, KAFKA_HOME是在前面安装kafka时配置的
### kfk env begin
## kafka
export KAFKA_HOME=/home/kafka/kafka_2.11-2.2.0
## kafka-eagle
export KE_HOME=/home/kafka/kafka-eagle-web-1.4.0
## PATH
export PATH=$PATH:$KAFKA_HOME/bin:$KE_HOME/bin
### kfk env end
```

- 使环境变量生效
```shell
# 重新加载 /etc/profile
source /etc/profile
# 验证是否生效
echo $KE_HOME
```

- 修改kafka-eagle配置文件
```shell
# 切换至配置目录
cd /home/kafka/kafka-eagle-web-1.4.0/conf
# 修改
vim system-config.properties
```
```shell
# 需关注如下配置信息

# 集群名称 cluster1 和后面的 cluster1.zk.list 的前缀(cluster1.)保持一致
# 目前仅一个集群, 先删除 ,cluster2
kafka.eagle.zk.cluster.alias=cluster1
# 修改 zk.lis 为你的实际kafka集群的zookeeper的集群信息
cluster1.zk.list=kafka01:2181,kafka02:2181,kafka03:2181
# 目前仅有一个 cluster1 , 先注释 cluster2
#cluster2.zk.list=xdn10:2181,xdn11:2181,xdn12:2181
# 老版本的 kafka 是将偏移量存放在zk, 新版本存放为 kafka 中, 故保持配置为 kafka
cluster1.kafka.eagle.offset.storage=kafka
# 需要注释 cluster2.kafka.eagle.offset.storage=zk, 暂时无 cluster2, 且当前kafka版本配置应为 kafka, 非zk
#cluster2.kafka.eagle.offset.storage=zk
# 默认不开启kafka健康报表图, 如果需要开启, 需在kafka启动时开启JMX监控
# 一般生产不开启
#kafka.eagle.metrics.charts=false
# 测试环境可验证一下
kafka.eagle.metrics.charts=true
# 可修改项, kafka 进行crud topic需要输入的密码
kafka.eagle.topic.token=keadmin

# 未使用的 cluster2集群 注释掉
#cluster2.kafka.eagle.sasl.enable=false
#cluster2.kafka.eagle.sasl.protocol=SASL_PLAINTEXT
#cluster2.kafka.eagle.sasl.mechanism=PLAIN
#cluster2.kafka.eagle.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka" password="kafka-eagle";
#cluster2.kafka.eagle.sasl.client.id=

# 不使用 sqlite 数据库, 需注释掉
#kafka.eagle.driver=org.sqlite.JDBC
#kafka.eagle.url=jdbc:sqlite:/hadoop/kafka-eagle/db/ke.db
#kafka.eagle.username=root
#kafka.eagle.password=www.kafka-eagle.org

# 使用mysql数据库, 打开mysql相关注释
kafka.eagle.driver=com.mysql.jdbc.Driver
# 数据库可以不用手动创建, kafka-eagle 启动时会创建
kafka.eagle.url=jdbc:mysql://192.168.25.11:3306/ke?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
kafka.eagle.username=root
kafka.eagle.password=123456

```

- 测试"检测Kafka性能指标"配置项, 需要修改Kafka启动文件
```shell
# 集群中的kafka均需修改对应配置信息, 增加 export JMX_PORT="9999" 配置
cd /home/kafka/kafka_2.11-2.2.0/bin/
vi kafka-server-start.sh
# 先找到如下启动信息配置
if [ "x$KAFKA_HEAP_OPTS" = "x" ]; then
    export KAFKA_HEAP_OPTS="-Xmx1G -Xms1G"
fi
```
```shell
# 替换成下列配置信息, 开启 JMX port
# 新增 export JMX_PORT="9999" 即可, 端口号随意
if [ "x$KAFKA_HEAP_OPTS" = "x" ]; then
    # 测试使用 G1 垃圾回收
    #export KAFKA_HEAP_OPTS="-Xmx1G -Xms1G"
    export KAFKA_HEAP_OPTS="-server -Xms1G -Xmx1G -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70"
    # 开启健康检查
    export JMX_PORT="9999"
fi
```

- 重启 kafka 集群
```shell
cd /home/kafka/kafka_2.11-2.2.0/bin/
# 停止
./kafka-server-stop.sh
# 启动
./kafka-server-start.sh -daemon ../config/server.properties
```

- 启动 kafka-eagle
```shell
/home/kafka/kafka-eagle-web-1.4.0/bin
# 添加执行权限
chmod +x ke.sh
# 启动
./ke.sh start
# 第一次启动需要创建数据库及创建表, 启动时间稍长
# 可查看日志, 直至显示 Server startup in 411954 ms
tail -888f /home/kafka/kafka-eagle-web-1.4.0/kms/logs/catalina.out
```

- 使用启动后的提示信息, 登录页面查看
http://192.168.25.106:8048/ke
Account: admin
Password: 123456
删除topic密码
keadmin
