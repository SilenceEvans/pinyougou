package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * 完成商品搜索功能的接口
 * @author wang
 */
public interface ItemSearchService {

    /**
     * 搜索的方法
     * @param searchMap 搜索的条件，map集合
     * @return 结果集合
     */
    public Map search(Map searchMap);

    /**
     * 往solr中新添加审核通过的spu所涵盖的sku的方法
     * @param items 新审核通过的spu所涵盖的sku中
     */
    void solrAddItems(List<TbItem> items);

    /**
     * 商品审核删除某商品时，同步删除solr中的数据
     * @param ids 要删除的spuId
     */
    void deleteSolrByGoodsId(Long[] ids);
}
