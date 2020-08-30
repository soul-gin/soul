##### 前置条件
- kafka 集群
##### 软件信息
- 网盘下载地址
链接：https://pan.baidu.com/s/1b5DjLQBmbz_8Nh7DyLj2BQ 
提取码：gin1 

##### install
- 当前安装演示版本
apache-flume-1.9.0-bin.tar.gz

- install
```shell
# 解压
tar -zxf apache-flume-1.9.0-bin.tar.gz -C /home/kafka/
# 查看
cd /home/kafka/apache-flume-1.9.0-bin/
ll
# 创建kafka消费flume收集信息的配置文件
cd conf
vim kafka.properties
```
```
# 添加如下配置信息
# Name the components on this agent
# define component name(input channel/pip output)
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# Describe/configure the source
# input
a1.sources.r1.type = avro
# alter begin 输入来源的hostname
a1.sources.r1.bind = kafka01
# alter end
a1.sources.r1.port = 44444

# Use a channel which buffers events in memory
# middle pip
a1.channels.c1.type = memory
a1.channels.c1.capacity = 10000
a1.channels.c1.transactionCapacity = 10000
a1.channels.c1.byteCapacityBufferPercentage = 20
a1.channels.c1.byteCapacity = 800000

# Describe the sink
# output
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
# alter begin 输出到哪个kafka的集群和tpoic
a1.sinks.k1.kafka.topic = topic01
a1.sinks.k1.kafka.bootstrap.servers = kafka01:9092,kafka02:9092,kafka03:9092
# alter end
a1.sinks.k1.kafka.flumeBatchSize = 20
a1.sinks.k1.kafka.producer.acks = -1
a1.sinks.k1.kafka.producer.linger.ms = 1
a1.sinks.k1.kafka.producer.compression.type = snappy

# Use a channel which buffers events in memory
# binding policy
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1

```

##### 测试
- 使用 eagle 来演示, 清理topic01(有则删除, 重新创建, 可以直接用kafka命令处理)
http://192.168.25.106:8048/ke
Account: admin
Password: 123456
删除topic密码
keadmin

- topic下面的 list 标签页
http://192.168.25.106:8048/ke/topic/list
点击remove(对应topic01), 输入 keadmin

- topic下面的 create 标签页
http://192.168.25.106:8048/ke/topic/create
创建 tpoic01 , 分区=3, 副本=1

- 查看topic01是否有数据
http://192.168.25.106:8048/ke/topic/message
select * from "topic01" where "partition" in (0,1,2) limit 10

- 启动kafka消费端
```shell
cd /home/kafka/kafka_2.11-2.2.0/bin
# 监听消费
./kafka-console-consumer.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --topic topic01
```
- 启动 flume
```shell
/home/kafka/apache-flume-1.9.0-bin/bin
# 启动, -n 指定agent名称, 和配置文件(a1.sources 前缀)保持一致
# -D 设置日志级别
bin/flume-ng agent -c conf/ -n a1 -f conf/kafka.properties -Dflume.root.logger=INFO,console
# 正常启动后不要关闭, 新开一个shell窗口操作
```
- 启动 flume avro-client 测试
```shell
# 切换目录
/home/kafka/apache-flume-1.9.0-bin/bin

# 生成测试文件
cat > testKafkaFlumeImput.log << 'EOF'
1a
2b
3c
4d
5e
66gin
77nier
88luffy
99naruto
EOF

# 启动, avro-client, 指定输入的 hostname port, 和flume 配置文件(kafka.properties中的a1.sources.r1)保持一致, --filename 指定输入文件
./flume-ng avro-client -c ../conf/ --host kafka01 --port 44444 --filename ./testKafkaFlumeImput.log
# 可以看到 kafka 刚启动的消费端消费了 testKafkaFlumeImput.log 中的数据
```

- 使用 eagle 来查看topic01是否有数据
http://192.168.25.106:8048/ke/topic/message
select * from "topic01" where "partition" in (0,1,2) limit 100