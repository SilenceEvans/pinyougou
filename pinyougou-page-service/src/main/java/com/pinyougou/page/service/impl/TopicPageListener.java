package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class TopicPageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        System.out.println("监听到生成页面的消息");
        try {
            itemPageService.genItemHtml(Long.parseLong(textMessage.getText()));
            System.out.println("页面已生成");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
