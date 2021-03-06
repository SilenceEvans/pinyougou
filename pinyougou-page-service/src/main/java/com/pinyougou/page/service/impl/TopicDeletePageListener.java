package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class TopicDeletePageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {

            Long[] goodsId = (Long[]) objectMessage.getObject();
            boolean b = itemPageService.deleteItemHtml(goodsId);
            System.out.println(b);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
