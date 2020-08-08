##### 安装
**前置条件**
- 安装jdk1.8+, 配置 JAVA_HOME
通过shell安装链接: [install_jdk.sh](https://blog.csdn.net/zxczb/article/details/106083420).
- 配置主机名和IP映射(需特别注意!!!)
```shell
# 优先配置解析
vim /etc/hosts
192.168.25.106 kafka01
192.168.25.107 kafka02
192.168.25.108 kafka03

# 临时生效
hostname kafka01

# 再配合永久生效修改, 避免重启实效
hostnamectl set-hostname kafka01
或(两种方式等价, 上面命令会直接修改 /etc/hostname 文件)
vim /etc/hostname 
kafka01

```
- 关闭防火墙( CentOS7 , 每台服务器都需要 )
```shell
# 停止firewall
systemctl stop firewalld.service
# 禁止firewall开机启动
systemctl disable firewalld.service
# 查看firewall状态( Active: inactive (dead) )
systemctl status firewalld.service
```
- 时钟同步
```shell
# 安装 ntp 服务
yum install -y ntp
# 设定时钟同步服务器同步 ntpdate cn.pool.ntp.org | ntp[1-7].aliyun.com
ntpdate ntp1.aliyun.com
# 同步时钟
clock -w
# 查看当前时间
date
```
- 安装&启动 zookeeper(zookeeper-3.4.6.tar.gz)
通过shell安装链接: [install_zookeeper.sh](https://blog.csdn.net/zxczb/article/details/106126349).
- 安装&启动 kafka(kafka_2.11-2.2.0.tgz)

**安装脚本**
install_kafka.sh
```shell
#!/bin/bash

###############################################
#
#   GIN INSTALL KAFKA ENV
#	需要配置 INSTALL_PATH
#   执行shell需要参数$1(集群中broker序号), 本机主机名, 例: ./kfk_install.sh 1 kafka01
#	config INSTALL_PATH (for broker id), hostname, execute like: ./kfk_install.sh 1 kafka01
#   需要配置 ZOOKEEPER_CLUSTER 单台则配置一个主机名:端口即可
#	configure ZOOKEEPER_CLUSTER, you can configure a hostname:port for a single
###############################################

# install_kfk_path
INSTALL_PATH='/home/kafka'
### Kafka links zookeeper cluster
ZOOKEEPER_CLUSTER='kafka01:2181,kafka02:2181,kafka03:2181'

### -------- 脚本入参项 --------
### 表示安装第几台,broker id 会和该值一样
### number of current machinei, such as: sh kfk_install.sh 1 kafka01
MACHINE_ID=$1
### 表示当前机器的主机名, 不能使用IP
### Indicates the host name of the current machine. IP cannot be used
LOACL_HOSTNAME=$2

### -------- 无需修改项 --------
PKG_NAME='kafka_2.11-2.2.0.tgz'
DIR_NAME='kafka_2.11-2.2.0'



function check_jdk()
{
	### kfk install need JAVA_HOME.  
	if [[ ! -d $JAVA_HOME ]];
	then
		echo "JAVA_HOME not set"
		exit 1
	else
		echo "JAVA_HOME=$JAVA_HOME"
	fi
} 

function check_package()
{
	### check install_path exists
	if [ ! -d "${INSTALL_PATH}" ];
	then
		echo "${INSTALL_PATH} not exit, mkdir"
		mkdir -p "${INSTALL_PATH}"
	else
		echo "${INSTALL_PATH} is exit"
	fi
	### get .tar.gz package name from current file
    PKG_NAME=`ls | grep kafka | grep .tgz`
	
	### check package
    if [ ! -f "${PKG_NAME}" ]
    then
		echo "you need install package!"
        exit
    fi    
 
    ### check unzip tmp dir
	DIR_NAME=`ls -l | grep '^d' |grep kafka |awk '{print$9}'`
	if [ -d "${DIR_NAME}" ];
    then
		echo "${DIR_NAME} is exit, rm unzip path"
        rm -rf "${DIR_NAME}"
	else
		echo "DIR_NAME is ok"
    fi
}

function install_info(){
	### execute shell param confirm 1
	if [ ! -z "$MACHINE_ID" ]; 
	then
		echo 'current MACHINE_ID='$MACHINE_ID
	else 
		### default value
		MACHINE_ID="0"
		echo "empty input param('$1'),using default 1" 
	fi
	
	### execute shell param confirm 2
	if [ ! -z "$LOACL_HOSTNAME" ]; 
	then
		echo 'current LOACL_HOSTNAME='$LOACL_HOSTNAME
	else 
		### default value
		LOACL_HOSTNAME="kafka01"
		echo "empty input param('$2'),using default kafka01" 
	fi
	
	echo
	echo "INSTALL_PATH: ${INSTALL_PATH}"
	echo "MACHINE_ID: ${MACHINE_ID}"
	echo "LOACL_HOSTNAME: ${LOACL_HOSTNAME}"
	echo "ZOOKEEPER_CLUSTER: ${ZOOKEEPER_CLUSTER}"
	echo

	while true; do
	    read -p "Check that the configuration, press [y/n] to continue: " yn
	    case $yn in
	        [Yy]* ) break;;
	        [Nn]* ) exit;;
	        * ) echo "please input y/n.";;
	    esac
	done
}

function install_kfk(){
	tar -xf $PKG_NAME
	### get file name
	DIR_NAME=`ls -l | grep '^d' |grep kafka |awk '{print$9}'`
	mv $DIR_NAME $INSTALL_PATH
	
	### kafka path
	TARGET_PATH=$INSTALL_PATH/$DIR_NAME
	
	### configuration file path and data path
	kfk_conf=$TARGET_PATH/config/server.properties
	kfk_data=$TARGET_PATH/data
	
	### config cluster
	### cluster id
	sed -i 's|broker.id=0|broker.id='$MACHINE_ID'|g' $kfk_conf
	### cluster hostname 
	sed -i 's|#listeners=PLAINTEXT://:9092|listeners=PLAINTEXT://'$LOACL_HOSTNAME':9092|g' $kfk_conf
	### cluster log data path
	mkdir -p $kfk_data
	sed -i 's|log.dirs=/tmp/kafka-logs|log.dirs='$kfk_data'|g' $kfk_conf
	### Kafka links zookeeper cluster
	sed -i 's|zookeeper.connect=localhost:2181|zookeeper.connect='$zookeeper_cluster'|g' $kfk_conf
	
	 
	### config env
	if [[ -z $KAFKA_HOME ]];then
		echo "### kfk env begin" >> /etc/profile
		echo "export KAFKA_HOME=$TARGET_PATH" >> /etc/profile
### use '' avoid $PATH resolved to actual value,if used "" $PATH need to be escaped like \$PATH
		echo 'export PATH=$PATH:$KAFKA_HOME/bin' >> /etc/profile
		echo "### kfk env end" >> /etc/profile
	fi
	
	### start kfk
	echo
	echo "you can use command to start kfk..."
	echo "$TARGET_PATH/bin/kafka-server-start.sh -daemon $TARGET_PATH/config/server.properties"
	echo "or"
	echo "cd $TARGET_PATH/"
	echo "bin/kafka-server-start.sh -daemon config/server.properties"
	echo
}

 
function main()
{
	### Execute as needed
	check_jdk
	check_package
    install_info
	install_kfk
}
 
# Execute main method 
main
# END


```

**单机安装**
- 将 kafka_2.11-2.2.0.tgz 和 install_kafka.sh 放在同一个目录下
- 配置 INSTALL_PATH 和 ZOOKEEPER_CLUSTER
- 执行 ./install_kafka.sh 1 kafka01

**集群安装**
- 将 kafka_2.11-2.2.0.tgz 和 install_kafka.sh 放在同一个目录下
- 配置 INSTALL_PATH 和 ZOOKEEPER_CLUSTER
- 每台机器分别执行:
> ./install_kafka.sh 1 kafka01
> ./install_kafka.sh 2 kafka02
> ./install_kafka.sh 3 kafka03

**单机测试**
- 创建 topic 
```shell
# 查看命令参数
kafka-topics.sh --help
# 指定server端hostname及port, 指定分区数, 指定副本数, 创建 topic, 名称为 topic01
kafka-topics.sh --bootstrap-server kafka01:9092 --create --partitions 2 --replication-factor 2 --topic topic01
# 上述命令执行会报 Error while executing topic command : org.apache.kafka.common.errors.InvalidReplicationFactorException: Replication factor: 2 larger than available brokers: 1. 的错误
# 因为目前为单机, broker数量=1, 副本数不应该大于broker的数量(大于则多个副本存放一台服务器, 该服务器宕机, 多个副本均失效, 失去了副本容灾的意义)
# 创建 topic
kafka-topics.sh --bootstrap-server kafka01:9092 --create --partitions 2 --replication-factor 1 --topic topic01
# 查看 topic
kafka-topics.sh --bootstrap-server kafka01:9092 --list
```
- 生产消费 topic 
```shell
# 打开5个shell窗口, 分别执行下列命令
# 分组1消费者1: kafka按分组进行消费, 需指定组名
kafka-console-consumer.sh --bootstrap-server kafka01:9092  --topic topic01 --group group01 
# 分组1消费者2: kafka按分组进行消费, 需指定组名
kafka-console-consumer.sh --bootstrap-server kafka01:9092  --topic topic01 --group group01 
# 分组1消费者3: kafka按分组进行消费, 需指定组名
kafka-console-consumer.sh --bootstrap-server kafka01:9092  --topic topic01 --group group01 
# 分组2消费者1: kafka按分组进行消费, 需指定组名
kafka-console-consumer.sh --bootstrap-server kafka01:9092  --topic topic01 --group group02 
# 生产者: 指定 --broker-list 
kafka-console-producer.sh --broker-list kafka01:9092  --topic topic01

# 测试1
# 在 producer 所在shell依次输入 1, 2, 3
# 可以看到: 
# "分组1消费者1" 输出了 1, 3
# "分组1消费者2" 输出了 2
# "分组1消费者3" 什么都没输出(没有分配到分区, 目前分区数为2)
# "分组2消费者1" 输出了 1, 2, 3

# 测试2
# 在 "分组1消费者1" 所在shell窗口, 按下 ctrl+c
# 再在 producer 所在shell依次输入 01, 02, 03
# 可以看到: 
# "分组1消费者1" 停止了服务
# "分组1消费者2" 输出了 01, 03
# "分组1消费者3" 输出了 02
# "分组2消费者1" 输出了 01, 02, 03

# 可知 kafka 按group(分组), 进行广播消息, 消息根据 consumer 数量进行轮询消费
# 一般 consumer数量 等于分区数, 超过分区数的consumer数量的服务器可以作为备机
# 当分配到分区的服务器宕机, 备机可以分配到分区进行消费

```

**集群测试**
- topic  创建
```shell
# 查看 topic
kafka-topics.sh --bootstrap-server kafka01:9092 --list
# 删除单机测试的 tipic
kafka-topics.sh --bootstrap-server kafka01:9092 --delete --topic topic01
# 如果存在 __consumer_offsets 也删除
kafka-topics.sh --bootstrap-server kafka01:9092 --delete --topic __consumer_offsets
# 查看 topic 是否删除完成
kafka-topics.sh --bootstrap-server kafka01:9092 --list
```

```shell
# 创建集群 topic, 这时 replication-factor 就可以等于2或3 (小于或等于 broker数量 即可)
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --create --partitions 3 --replication-factor 2 --topic topic01

# 查看 topic
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --list

# 查看 topic 对应的数据目录
ll -h /home/kafka/kafka_2.11-2.2.0/data/
# 发现不同服务器分别为
# kafka01: topic01-1  topic01-2
# kafka02: topic01-0  topic01-2
# kafka02: topic01-0  topic01-1
# 以上为副本数为 2 的备份

# 通过命令查看
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --describe --topic topic01
# 信息如下( Configs:segment 文件段大小, 默认1G, 单位字节 )
# Topic:topic01   PartitionCount:3        ReplicationFactor:2     Configs:segment.bytes=1073741824
# Topic: topic01  Partition: 0    Leader: 2       Replicas: 2,3   Isr: 2,3
# Topic: topic01  Partition: 1    Leader: 3       Replicas: 3,1   Isr: 3,1
# Topic: topic01  Partition: 2    Leader: 1       Replicas: 1,2   Isr: 1,2
```

```shell
# 创建分区数=2, 副本数=3的 topic02
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --create --partitions 2 --replication-factor 3 --topic topic02
# 查看 topic02 信息
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --describe --topic topic02
# 当 分区数 = broker数时, Leader会均匀分配到每个broker
# 当 分区数 < broker数时, 部分broker分配不到Leader
# 当 副本数 = broker数时, 每个broker分配到所有的Replicas
# 当 分区数 < broker数时, 每个broker分配到部分的Replicas
```
- topic  修改
```shell
# 分区数只能增加, 不能减少
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --alter --partitions 1 --topic topic02
# 会报 Error while executing topic command : org.apache.kafka.common.errors.InvalidPartitionsException: Topic currently has 2 partitions, which is higher than the requested 1. 的错误信息

# 增加分区数
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --alter --partitions 3 --topic topic02
# 查看 topic02 信息
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --describe --topic topic02
```

- topic  删除
```shell
# 删除 topic
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --delete --topic topic02
# 查看 topic 列表信息
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --list
```

- 生产消费 topic 
```shell
# kafka01 服务器, 创建消费组, 打印key, 打印value, 指定key和value的分割符
kafka-console-consumer.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --topic topic01 --group group01 --property print.key=true --property print.value=true --property key.separator=,
# kafka02 服务器, 创建生产者
kafka-console-producer.sh --broker-list kafka01:9092,kafka02:9092,kafka03:9092  --topic topic01
# 生产端输入 gin
# 消费端显示 null,gin  -- 目前没指定key, 所以为null
# kafka03 服务器, 查看 组,组详情 信息
kafka-consumer-groups.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --list
kafka-consumer-groups.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --describe --group group01
# 查看 topic, 发现多了个 __consumer_offsets
kafka-topics.sh --bootstrap-server kafka01:9092,kafka02:9092,kafka03:9092 --list
```