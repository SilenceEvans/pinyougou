package com.pinyougou.solrUtil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    public void importItemData(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //查询已审核的商品
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        for (TbItem tbItem : itemList){
            //将规格列转为json对应的Map格式
            Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring" +
                "/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil)context.getBean("solrUtil");
        //solrUtil.importItemData();
        solrUtil.deleteSlor();
    }

    //新添加一个删除slor中所有数据的方法
    private void deleteSlor(){
        Query simpleQuery = new SimpleQuery("*:*");
        solrTemplate.delete(simpleQuery);
        solrTemplate.commit();
    }
}
