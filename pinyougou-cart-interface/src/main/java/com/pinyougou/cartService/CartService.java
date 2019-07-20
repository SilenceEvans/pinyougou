package com.pinyougou.cartService;

import com.pinyougou.pojo.Cart;

import java.util.List;

public interface CartService {

    /**
     * 新增商品至购物车的方法
     * @param cartList 查询到的原有的购物车列表集合
     * @param itemId skuId
     * @param num 添加的数量
     * @return 新增加商品后的购物车列表集合
     */
     List<Cart> addGoodsToCart(List<Cart> cartList, Long itemId, Integer num);


    /**
     * 根据用户名在缓存中查找对应的购物车
     * @param username 用户名
     * @return
     */
     List<Cart> findCartInRedis(String username);

    /**
     * 将cookie中的购物车与缓存中的购物车合并
     * @param cartList_cookie cookie中存放的购物车
     * @param cartInRedis 缓存中存放的购物车
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartInRedis);

    /**
     * 将合并后的购物车存入缓存
     * @param cartList
     */
    void saveCartInRedis(List<Cart> cartList,String username);
}

