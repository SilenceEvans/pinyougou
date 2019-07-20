package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cartService.CartService;
import com.pinyougou.pojo.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/cart")
@RestController
public class CartController {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference(timeout=6000)
    private CartService cartService;

    @RequestMapping("/findAllCart")
    public List<Cart> findAllCart(){
        //通过security的api获得登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //先从cookie中查找购物车中存储的订单
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //判断其是否登录，如果未登录只从cookie中查
        if (cartListString == null || "".equals(cartListString)){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        //判断用户是否登录
        if ("anonymousUser".equals(username)){
            System.out.println("从cookie中查数据");
            return cartList_cookie;
        }else {
            //从缓存中查找
            List<Cart> cartInRedis = cartService.findCartInRedis(username);
            //如果cartList_cookie中没有数值，则返回缓存中的值
            if (cartList_cookie.size()==0){
                return cartInRedis;
            }
            //调用cartService中合并缓存中购物车与cookie中购物车的方法
            List<Cart> cartList = cartService.mergeCartList(cartList_cookie,cartInRedis);
            //清除cookie中的数据
            CookieUtil.deleteCookie(request,response,"cartList");
            //将cartList存入缓存
            cartService.saveCartInRedis(cartList,username);
            //返回cartList
            return cartList;
        }
    }

    @RequestMapping("/addGoodsToCart")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCart(Long itemId,Integer num){
        //设置商品详情页所在的域可以访问本域
        //response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
        //允许携带凭证即cookie
        //response.setHeader("Access-Control-Allow-Credentials","true");
        //通过security的api获得登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            //先从已存在的购物车列表中，查找是否有对应的商家id的订单存在
            List<Cart> cartList = findAllCart();
            cartList = cartService.addGoodsToCart(cartList,itemId,num);
            if ("anonymousUser".equals(username)){
                System.out.println("没登录，存储到cookie");
                //如果没登录，将重新完成添加的订单存储到与查询同名的订单中，该订单在cookie中
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),
                        3600*24,"utf-8");
            }else {
                //如果登陆了，新添加的订单存储到redis中
                cartService.saveCartInRedis(cartList,username);
            }
            return new Result(true,"添加到购物车成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"服务器出小差了,稍后再试！");
        }

    }
}
