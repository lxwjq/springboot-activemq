package com.example.demo.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;

/**
 * MQ配置
 * <p>
 * ACK_MOD的几种类型：
 * int AUTO_ACKNOWLEDGE = 1;自动确认
 * int CLIENT_ACKNOWLEDGE = 2;客户端手动确认
 * int DUPS_OK_ACKNOWLEDGE = 3;批量自动确认
 * int SESSION_TRANSACTED = 0;事物提交确认
 * int INDIVIDUAL_ACKNOWLEDGE = 4（单条消息确认）
 *
 * @author lixiang
 * @version V1.0
 * @date 2020/9/16 16:52
 **/
@Configuration
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.topic-name}")
    private String password;

    @Value("${spring.activemq.queue-name}")
    private String queueName;

    @Value("${spring.activemq.topic-name}")
    private String topicName;

    @Value("${spring.activemq.pool.max-connections}")
    private Integer maxConnections;

    @Bean(name = "queue")
    public Queue queue() {
        return new ActiveMQQueue(queueName);
    }

    @Bean(name = "topic")
    public Topic topic() {
        return new ActiveMQTopic(topicName);
    }


    /**
     * 消息重发机制RedeliveryPolicy
     * <p>
     * 什么情况下会触发消息重发
     * 1．在使用事务的Session中，调用rollback()方法；
     * 2．在使用事务的Session中，调用commit()方法之前就关闭了Session;
     * 3．在Session中使用CLIENT_ACKNOWLEDGE签收模式或者INDIVIDUAL_ACKNOWLEDGE模式，并且调用了recover()方法。
     * 可以通过设置ActiveMQConnectionFactory来定制想要的再次传送策略。
     *
     * @date 2020/9/17 14:22
     * @author lixiang
     **/
    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        //是否在每次尝试重新发送失败后,增长这个等待时间
        redeliveryPolicy.setUseExponentialBackOff(true);
        //重发次数,默认为6次   这里设置为10次
        redeliveryPolicy.setMaximumRedeliveries(10);
        //重发时间间隔,默认为1秒
        redeliveryPolicy.setInitialRedeliveryDelay(1);
        //第一次失败后重新发送之前等待500毫秒,第二次失败再等待500 * 2毫秒,这里的2就是value
        redeliveryPolicy.setBackOffMultiplier(2);
        //是否避免消息碰撞
        redeliveryPolicy.setUseCollisionAvoidance(false);
        //设置重发最大拖延时间-1 表示没有拖延只有UseExponentialBackOff(true)为true时生效
        redeliveryPolicy.setMaximumRedeliveryDelay(-1);
        return redeliveryPolicy;
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(RedeliveryPolicy redeliveryPolicy) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(username, password, brokerUrl);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        return activeMQConnectionFactory;
    }

    @Bean
    public PooledConnectionFactory pooledConnectionFactory(ActiveMQConnectionFactory activeMQConnectionFactory){
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(maxConnections);
        return pooledConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(PooledConnectionFactory pooledConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        //进行持久化配置 1表示非持久化，2表示持久化
        jmsTemplate.setDeliveryMode(2);
        jmsTemplate.setConnectionFactory(pooledConnectionFactory);
        //客户端签收模式
        jmsTemplate.setSessionAcknowledgeMode(4);
        return jmsTemplate;
    }

    /**
     * 定义一个消息监听器连接工厂，这里定义的是点对点模式的监听器连接工厂
     *
     * @date 2020/9/17 14:54
     * @author lixiang
     **/
    @Bean(name = "queueListener")
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(PooledConnectionFactory pooledConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(pooledConnectionFactory);
        //设置连接数
        factory.setConcurrency("1-10");
        //重连间隔时间
        factory.setRecoveryInterval(1000L);
        factory.setSessionAcknowledgeMode(4);
        return factory;
    }

    /**
     * 定义一个消息监听器连接工厂，这里定义的是发布订阅模式的监听器连接工厂
     * topoic模式不支持ACK(待确认)
     *
     * @date 2020/9/17 14:54
     * @author lixiang
     **/
    @Bean(name = "topicListener")
    public DefaultJmsListenerContainerFactory jmsTopoicListenerContainerFactory(PooledConnectionFactory pooledConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(pooledConnectionFactory);
        //设置连接数
        factory.setConcurrency("1-10");
        //重连间隔时间
        factory.setRecoveryInterval(1000L);
        factory.setSessionAcknowledgeMode(4);
        factory.setPubSubDomain(true);
        return factory;
    }
}
