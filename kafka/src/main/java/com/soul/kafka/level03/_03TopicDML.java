package com.soul.kafka.level03;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class _03TopicDML {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //配置连接参数
        Properties props = new Properties();
        //配置主机名-IP映射解析, C:\Windows\System32\drivers\etc\hosts
        /*
#kafka node begin
192.168.25.106 kafka01
192.168.25.107 kafka02
192.168.25.108 kafka03
#kafka node end
         */
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka01:9092,kafka02:9092,kafka03:9092");

        KafkaAdminClient adminClient = (KafkaAdminClient) KafkaAdminClient.create(props);

        //删
        //delete(adminClient, "topic01", "topic02");
        delete(adminClient, "topic02");

        //等待删除完成
        Thread.sleep(2000);

        //增
        create(adminClient, "topic02");

        //查
        query(adminClient);

        //查, 详情
        queryDetail(adminClient, "topic02");

        adminClient.close();
    }

    private static void query(KafkaAdminClient adminClient) throws InterruptedException, ExecutionException {
        //查询topics
        KafkaFuture<Set<String>> nameFutures = adminClient.listTopics().names();
        System.out.println("-------- query begin --------");
        for (String name : nameFutures.get()) {
            System.out.println(name);
        }
        System.out.println("-------- query end --------");
    }

    private static void queryDetail(KafkaAdminClient adminClient, String... topicName) throws InterruptedException,
            ExecutionException {
        //查看Topic详情
        DescribeTopicsResult describeTopics =
                adminClient.describeTopics(Arrays.asList(topicName));
        Map<String, TopicDescription> tdm = describeTopics.all().get();
        System.out.println("-------- query detail begin --------");
        for (Map.Entry<String, TopicDescription> entry : tdm.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        System.out.println("-------- query detail end --------");
    }

    private static void delete(KafkaAdminClient adminClient, String... topicNameList) {
        //删除Topic
        adminClient.deleteTopics(Arrays.asList(topicNameList));
    }

    private static void create(KafkaAdminClient adminClient, String topicName) {
        //分别指定 topic名称, 分区数, 副本因子
        List<NewTopic> newTopics = Arrays.asList(new NewTopic(topicName, 2, (short) 3));
        //创建Topics
        //同步
        //adminClient.createTopics(newTopics);

        //异步
        CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopics);
        //调用下面方法会将异步创建改成同步创建
        try {
            createTopicsResult.all().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


}
