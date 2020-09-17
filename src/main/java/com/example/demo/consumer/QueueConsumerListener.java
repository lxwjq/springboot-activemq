package com.example.demo.consumer;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * queue模式的消费者
 *
 * @author lixiang
 * @version V1.0
 * @date 2020/9/16 16:51
 **/
@Component
public class QueueConsumerListener {

    private static int num = 0;

    @JmsListener(destination = "${spring.activemq.queue-name}", containerFactory = "queueListener")
    public void readActiveQueue(TextMessage text, Session session) {
        try {
            System.out.println("queue接受到：" + text.getText());
            // 使用手动签收模式，需要手动的调用，如果不在catch中调用session.recover()消息只会在重启服务后重发
//            text.acknowledge();
            if (num >= 3) {
                text.acknowledge();
            } else {
                num++;
                System.out.println("本次没有ack，抛出异常进入重试机制");
                throw new RuntimeException("no acknowledge error");
            }
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
