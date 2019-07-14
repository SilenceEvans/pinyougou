package com.pinyougou.search.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听到消息");
        TextMessage textMessage = (TextMessage) message;
        String text = null;
        try {
            text = textMessage.getText();
            List<TbItem> items = JSON.parseArray(text, TbItem.class);
            for (TbItem item : items){
                System.out.println(item.getId());
                Map specMap = JSON.parseObject(item.getSpec());
                item.setSpecMap(specMap);
            }
            itemSearchService.solrAddItems(items);
            System.out.println("成功放到索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
