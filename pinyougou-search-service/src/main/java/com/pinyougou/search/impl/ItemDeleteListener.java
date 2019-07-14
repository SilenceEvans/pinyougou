package com.pinyougou.search.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodIds = (Long[]) objectMessage.getObject();
            System.out.println("获得需要删除的索引信息");
            itemSearchService.deleteSolrByGoodsId(goodIds);
            System.out.println("成功删除索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
