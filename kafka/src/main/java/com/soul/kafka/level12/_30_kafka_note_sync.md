##### kafka
- Segments
> Kafka的Topic被分为多个分区，分区是是按照Segments存储文件块(默认大小为1G, 超过1G则形成新的Segments)。

> 分区日志是存储在磁盘上的日志序列，Kafka可以保证分区里的事件是有序的。其中Leader负责对应分区的读写、Follower负责同步分区的数据

- LEO：log end offset 
> 标识的是每个分区中最后一条消息的下一个位置，分区的每个副本都有自己的 LEO 

- HW: high watermarker
> 称为高水位线(同步数据与未同步数据的分界线, 可以理解为同步至第几条消息或同步至哪个offset)，所有HW之前的的数据都理解是已经备份的,当所有节点都备份成功，Leader会更新水位线。

- ISR:In-sync-replicas
>kafka的leader会维护一份处于同步的副本集和，如果在`replica.lag.time.max.ms`时间内系统没有发送fetch请求，或者已然在发送请求，但是在该限定时间内没有赶上Leader的数据就被剔除ISR列表。

>在Kafka-0.9.0版本剔除`replica.lag.max.messages`消息个数限定，因为这个会导致其他的Broker节点频繁的加入和退出ISR。(通过lag数字值(各自维护)是否一致来判断同步是否成功, 高并发下很容易略大于Leader, 则follower会频繁被下线)

- 同步机制(0.11 版本之前)
> 0.11 版本之前Kafka使用highwatermarker机制保证数据的同步，但是基于highwatermarker的同步数据(高水位截断)可能会导致数据的不一致或者是乱序。

> 丢失场景(一主一从): 
> 1. leader和follower均读取到了msg1和msg2; 
> 2. leader写入了msg1 和 msg2, leader的高水位位于msg2(HW=2); follower仅写入了msg1, 高水位位置位于msg1(HW=1); 
> 3. follower重启, 发现高水位位于msg1, 则截断msg1(HW=1)后面的数据msg2(HW=2), leader正常; 
> 4. follower重启触发截断时, leader正好宕机, follower升级为 new leader, 则msg2丢失了, 新的消息msg3开始被读取到new leader并开始同步

> 数据不一致场景(一主一从): 
> 1. leader读取写入msg1和msg2, follower均读取写入msg1, 未读取写入msg2; 
> 2. leader同步了msg1 和 msg2, leader的高水位位于msg2(HW=2); follower仅同步了msg1, 高水位位置位于msg1(HW=1); 
> 3. follower重启, leader宕机; 
> 4. follower升级为 new leader, 新的消息msg3开始被读取到new leader并同步成功(HW=2)
>  5. 此时 old leader重启成为new follower, 发现和new leader的HW相等, 则不同步, 但此时HW=2的数据分别是msg3(new leader) 和 msg2(new follower)

- 同步机制(0.11 版本及之后)
>0.11版本之前Kafka的副本备份机制的设计存在数据不一致问题和丢失数据问题，因此Kafka-0.11版本引入了 Leader Epoch(Epoch 时代, 纪元; `大人时代变了, 要开启新的纪元了`)解决这个问题，不再使用HW作为数据截断的依据。

**由Controller维护在zookeeper中的版本号**
>任意一个Leader持有一个LeaderEpoch(可以理解成递增版本号), 这是一个由Controller(新增服务)管理的32位单调递增的数字，存储在Zookeeper的分区状态信息中，并作为LeaderAndIsrRequest的一部分传递给每个new Leader。

**消息携带版本号, Leader Epoch Sequence文件维护版本号与该版本第一条消息的开始offset的映射关系**
>改进消息格式，每个消息集都带有一个4字节的Leader Epoch号。
>在每个日志目录中，会创建一个新的Leader Epoch Sequence文件(leader follower各自维护自己的)，在其中存储Leader Epoch的序列和在该Epoch中生成的消息的Start Offset。它也缓存在每个副本中，也缓存在内存中。

**同步流程**
>初代(第0版本)的Leader接受Producer第一次请求数据, broker写入时使用当前leader的LeaderEpoch(目前为0)给消息标记Leader Epoch号。同时在LeaderEpochSequence文件中记录(0, 0)第一个0表示第0版本, 第二个0表示当前版本的起始消息的start offset为0。

> follower(0, 0)刚启动, 向leader请求恢复, leader发现自己也是(0,0), 当前消息处理到offset=99的位置,  则返回follower (0, 99); 此时follower在收到(0, 99)时, 比对自己的(0,0)则向leader同步offset在0-99区间内的消息; 

> 假如此时leader写入消息至offset=200的位置, 宕机了; 此时通过选举follower升级为new leader, controller将zookeeper中分区信息+1, 保存的是(0+1=1 表示初代皇帝驾崩了, 新皇帝来登基了), new leader则从刚同步完成的位置offset=50(假设上次没同步完成, 即消息还没同步到offset=99)标记结束(LEO), 从(1,51)开始作为new leader接收消息(将Leader Epoch = 1写入新接收的消息中)

> 如果此时宕机的leader恢复了, 成为new follower, 需要从向相应的分区的new Leader发送epoch请求，该请求包含最新的Leader Epoch,StartOffset信息(1,51), 发现需要从offset=51开始截断(历史的51-200未成功同步至副本(old follower)的消息被清理), 并从new leader开始同步

> 处理丢失场景(一主一从): 
> 1. leader和follower均读取到了msg1和msg2; 
> 2. leader写入了msg1 和 msg2是(0, 0); follower仅写入了msg1也是(0, 0); 
> 3. follower重启(0, 0), leader正常(0, 0); 
> 4. follower重启时, leader正好宕机, follower升级为 new leader, 通过controller向zookeeper获得到Leader Epoch = 0, 则new leader从当前读取到的结束offset截断(截至msg2结束位置), 新的消息msg3开始被读取到new leader并开始同步, 消息的Leader Epoch = 1, 即(1, 3), 即避免了msg2的丢失问题

> 数据不一致场景(一主一从): 
> 1. leader读取写入msg1和msg2, follower均读取写入msg1, 未读取写入msg2; 
> 2. leader同步了msg1 和 msg2是(0, 0); follower仅同步了msg1也是(0, 0); 
> 3. follower重启, leader宕机; 
> 4. follower升级为 new leader(1, 1), 新的消息msg3(offset=1)开始被读取到new leader
> 5. 此时 old leader重启成为new follower,  通过controller向zookeeper获得到Leader Epoch = 1, 从new leader请求恢复, 拉取到(1, 1), 则会只保留msg1, 将msg2删除, 同步从offset=1的位置开始从new leader同步消息(msg3会替换掉原来offset=1的msg2, 避免了数据不一致问题)

- 同步机制区别
> High Watermarker通过HW数字值进行粗暴截断, 不关心区间内数据是否变动, 只关心结束位置
> Leader Epoch除了关心结束位置, 还关心每个区间内的数据应该使用哪个版本的

> 如果从两家族分赃策略来类比上面两种策略:
>  High Watermarker, R家族和E家族, 达成共识的就一致确定并分赃(同步), 未达成共识的则各自持有保留态度(不一致)
>  Leader Epoch, R家族和E家族打一架,  R家族赢了, 则2000年(leader, Leader Epoch=0)开始听R家族的分赃计划(start offset),  突然2008年R家族被绊了一跤(宕机), 从2008年(成新的leader, Leader Epoch + 1, 开启新时代)开始按E家族的分赃计划(start offset)继续执行(R家族制定2008年之后的分赃计划全部作废, end offset)