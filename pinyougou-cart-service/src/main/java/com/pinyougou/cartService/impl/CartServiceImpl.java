package com.pinyougou.cartService.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cartService.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCart(List<Cart> cartList, Long itemId, Integer num) {
        //根据itemId查询SellerId,遍历cartList看是否已存在对应sellerId的购物车项
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        if (item == null){
            throw new RuntimeException("该商品不存在！");
        };
        if ("0".equals(item.getStatus())){
            throw new RuntimeException("该商品状态无效!");
        }
        //根据查询到的sku获取sellerId
        String sellerId = item.getSellerId();
        String sellerName = item.getSeller();
        Cart cart = searchCartInCartList(cartList, sellerId);
        if (cart!=null){
            //如果存在对应的购物车项，返回该购物车项
            //再遍历购物车项中的订单集合，是否存在对应的itemId
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //如果存在则更改对应的订单的数量
            TbOrderItem orderItem = searchItemInOrderItemListByItemId(orderItemList, itemId);
            if (orderItem!=null){
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                if (orderItem.getNum()<=0){
                    orderItemList.remove(orderItem);
                }
                if (orderItemList.size()==0){
                    cartList.remove(cart);
                }
            }else{
                //如果不存在，新增此商品sku对应的订单
                orderItemList.add(addOrderItem(item,num));
            }
        }else {
            //如果不存在，创建新的购物车项
            Cart addCart = new Cart();
            addCart.setSellerId(sellerId);
            addCart.setSellerName(sellerName);
            //创建新的订单集合并往其中添加订单
            List<TbOrderItem> tbOrderItems = new ArrayList<TbOrderItem>();
            tbOrderItems.add(addOrderItem(item,num));
            addCart.setOrderItemList(tbOrderItems);
            cartList.add(addCart);
        }
        return cartList;
    }

    /**
     * 判断要往购物车中添加的商家是否在购物车项中已存在
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartInCartList(List<Cart> cartList, String sellerId){
        for (Cart cart : cartList){
            if (cart.getSellerId().equals(sellerId)){
                //如果查询到该sellerId，返回该cart
                return cart;
            }
        }
        return null;
    }

    /**
     * 判断某个购物车项的订单集合中是否存在itemId对应的订单
     * @return
     */
    private TbOrderItem searchItemInOrderItemListByItemId
    (List<TbOrderItem> orderItemList, Long itemId){
        for (TbOrderItem orderItem : orderItemList){
            if (orderItem.getItemId().longValue() == itemId){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据sku设置订单信息
     * @param item sku
     * @return 单个订单列表
     */
    private TbOrderItem addOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    @Override
    public List<Cart> findCartInRedis(String username) {
        System.out.println("从缓存中拿取购物车");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartInRedis) {
        System.out.println("合并缓存和cookie中的订单");
        //遍历cartList_cookie
        for (Cart cartCookie:cartList_cookie){
            for (TbOrderItem orderItem:cartCookie.getOrderItemList()){
                cartInRedis = addGoodsToCart(cartInRedis, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartInRedis;
    }

    @Override
    public void saveCartInRedis(List<Cart> cartList,String username) {
        System.out.println("合并后的购物车放入缓存");
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }
}
