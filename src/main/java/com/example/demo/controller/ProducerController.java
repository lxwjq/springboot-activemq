package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 队列消息控制器
 *
 * @author lixiang
 * @version V1.0
 * @date 2020/9/16 16:39
 **/
@RestController
public class ProducerController {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @Autowired
    private Topic topic;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @GetMapping("/queue/test")
    public String sendQueue(String str) {
        this.sendMessage(this.queue, str);
        System.out.println(atomicInteger.incrementAndGet());
        return "success";
    }

    @GetMapping("/topic/test")
    public String sendTopic(String str) {
        this.sendMessage(this.topic, str);
        return "success";
    }

    /**
     * 发送消息，destination是发送到的队列，message是待发送的消息
     *
     * @param destination: 发送到的队列
     * @param message:     消息体
     * @date 2020/9/17 14:27
     * @author lixiang
     **/
    private void sendMessage(Destination destination, final String message) {
        jmsTemplate.convertAndSend(destination, message);
    }
}
