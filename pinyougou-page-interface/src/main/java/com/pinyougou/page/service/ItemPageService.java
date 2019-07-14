package com.pinyougou.page.service;

/**
 * 商品详细页的接口
 * @author wang
 */
public interface ItemPageService {
    /**
     * 根据spuId生成静态页面
     * @param goodsId spuId
     * @return 返回一个boolean类型的值
     */
    public boolean genItemHtml(Long goodsId);


    /**
     * 根据spuId删除无用的静态页面
     * @param goodsId spuId
     * @return 返回一个boolean类型的值
     */
    public boolean deleteItemHtml(Long[] goodsId);
}
