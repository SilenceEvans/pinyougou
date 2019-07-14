package com.pinyougou.search.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/**
 * ItemSearchService的实现类
 * @author solar wang
 */

@Service(timeout = 100000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();


        //去掉用户输入的信息中的空格
        String keywords = (String) searchMap.get("keywords");
        if (keywords!=null && keywords.length()>0){
            searchMap.put("keywords",keywords.replace(" ",""));
        }

        //根据搜索信息获得结果，搜索信息高亮显示
        Map searchList = searchList(searchMap);
        map.putAll(searchList);

        //根据搜索信息获得分类结果
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        /*根据searchMap中的category属性来进行判断，看是否其有值，没有值则根据第一个分类来展示信息，如果有值则根据这个值
          对应的种类来进行品牌及规格信息的展示
         */
        if (!"".equals(searchMap.get("category"))){
            String category = (String) searchMap.get("category");
            map.putAll(searchBrandAndSpecInRedis(category));
        }else {
            //根据分类结果的第一个分类显示品牌与规格信息
            //判断该集合是否有值
            if (categoryList.size() > 0){
                //调取获得品牌与规格信息的方法
                String category =(String)categoryList.get(0);
                map.putAll(searchBrandAndSpecInRedis(category));
            }
        }
        return map;
    }

    @Override
    public void solrAddItems(List<TbItem> items) {
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Override
    public void deleteSolrByGoodsId(Long[] ids) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(ids));
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 定义一个设置指定域高亮的方法(搜索关键字高亮)
     * @return 指定域字段高亮的集合
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //给查询对象设置高亮域
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //1.给查询对象设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //2.查询对象中设置分类过滤条件
        if (!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get(
                    "category"));
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }
        //3.查询对象中设置品牌过滤条件
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get(
                    "brand"));
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }

        //4.查询对象中设置规格过滤条件
        if (searchMap.get("spec")!=null){
            Map<String,String> spec = (Map) searchMap.get("spec");
            Set<String> keySets = spec.keySet();
            for (String key : keySets){
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(((Map) searchMap.get(
                        "spec")).get(key));
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }
        
        //5.查询对象中设置价格过滤条件
        if (!"".equals(searchMap.get("price"))){
            //将获得的价格以"-"进行分割
            String price = (String) searchMap.get("price");
            String[] prices = price.split("-");
            //添加价格大于第一个元素的筛选条件
            Criteria criteriaMin =
                    new Criteria("item_price").greaterThanEqual(prices[0]);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaMin);
            query.addFilterQuery(filterQuery);
            if (!"*".equals(prices[1])){
                //如果存在上限，则需添加上限条件
                Criteria criteriaMax =
                        new Criteria("item_price").lessThanEqual(prices[1]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(criteriaMax);
                query.addFilterQuery(simpleFilterQuery);
            }
        }
        //6.设置分页
        //提取页码
        Integer pageNo =(Integer)searchMap.get("pageNo");
        //提取每页的容量
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNo == null){
            pageNo = 1;
        }
        if (pageSize == null){
            pageSize = 20;
        }
        //设置从第几条记录开始查询
        query.setOffset((pageNo-1)*20);
        //设置容量
        query.setRows(pageSize);

        //7.添加排序查询
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if (sortValue!=null && !"".equals(sortValue)){
            if ("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮入口集合
        List<HighlightEntry<TbItem>> entryList = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList){
            //获取原实体类
            TbItem tbItem = entry.getEntity();
            if (entry.getHighlights().size()>0&&entry.getHighlights().get(0).getSnipplets().size()>0){
                tbItem.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",highlightPage.getContent());
        //放入总记录数
        map.put("total",highlightPage.getTotalElements());
        //方入总页数
        map.put("totalPage",highlightPage.getTotalPages());
        return map;
    }

    /**
     * 返回查询商品分类的集合
     * @param searchMap 搜索的内容
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<String>();
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        GroupPage<TbItem> itemGroupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> itemCategory = itemGroupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = itemCategory.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> groupEntry : content){
            String groupValue = groupEntry.getGroupValue();
            list.add(groupValue);
        }
        return list;
    }

    /**
     * 从缓存中查询模板与规格信息的方法
     * @param category 分类名称
     * @return 存储模板与规格信息的集合
     */
    private Map searchBrandAndSpecInRedis(String category){
        Map brandAndSpecMap = new HashMap();
        //从缓存itemCat中查询模板id
        Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
        //通过模板id从缓存中查询规格与品牌信息
        //判断typeId是否为空再进行查询
        if(typeId != null){
            List specList =(List) redisTemplate.boundHashOps("specList").get(typeId);
            List brandList = (List)redisTemplate.boundHashOps("brandList").get(typeId);
            brandAndSpecMap.put("specList",specList);
            brandAndSpecMap.put("brandList",brandList);
            return brandAndSpecMap;
        }else {
            return null;
        }
    }
}
