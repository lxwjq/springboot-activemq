package com.example.demo.consumer;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * topic模式的消费者
 *
 * @author lixiang
 * @version V1.0
 * @date 2020/9/16 16:52
 **/
@Component
public class TopicConsumerListener {

    @JmsListener(destination = "${spring.activemq.topic-name}", containerFactory = "topicListener")
    public void readActiveQueue(TextMessage text, Session session) {
        try {
            System.out.println("topic接受到：" + text.getText());
            // 使用手动签收模式，需要手动的调用，如果不在catch中调用session.recover()消息只会在重启服务后重发
            text.acknowledge();
        } catch (Exception e) {
            try {
                // 此不可省略 重发信息使用
                session.recover();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

    @JmsListener(destination = "${spring.activemq.topic-name}", containerFactory = "topicListener")
    public void readActiveQueue1(TextMessage text, Session session) {
        try {
            System.out.println("topic1接受到：" + text.getText());
            // 使用手动签收模式，需要手动的调用，如果不在catch中调用session.recover()消息只会在重启服务后重发
            text.acknowledge();
        } catch (Exception e) {
            try {
                // 此不可省略 重发信息使用
                session.recover();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }
}
