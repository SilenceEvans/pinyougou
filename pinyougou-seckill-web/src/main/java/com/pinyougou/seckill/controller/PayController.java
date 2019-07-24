package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.seckill.service.SeckillOrderService;
import com.pinyougou.pay.service.WeChatPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {
    @Reference(timeout = 10000)
    private WeChatPayService weChatPayService;
    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map creativeNative(){
        //获得登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名在缓存中查找对应的订单是否存在
        TbSeckillOrder seckillOrder = seckillOrderService.findOrderFromRedisByUsername(username);
        if (seckillOrder != null){
            //进行支付
            String total_fee=(long)(seckillOrder.getMoney().doubleValue()*100)+"";
            Map nativeMap = weChatPayService.createNative(seckillOrder.getId() + "",
                    total_fee);
            return nativeMap;
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获得登录信息
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int x = 0;
        while (true){
            Map map = weChatPayService.queryPayStatus(out_trade_no);
            if (map == null){
                //说明支付过程有问题
                return new Result(false,"支付失败！");
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                //如果支付成功，将订单保存至数据库
                seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),
                        (String)map.get("transaction_id"));
                return new Result(true,"支付成功！");
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            Result result = null;
            if (x>=10){
                result = new Result(false,"支付超时");
                //调用关闭订单状态的方法
                Map payResult = weChatPayService.closePay(out_trade_no);
                if (payResult != null&& "FAIL".equals(payResult.get("return_code"))){
                    //关闭状态中也有正常关闭
                    if ("ORDERPAID".equals(payResult.get("err_code"))){
                        //正常执行修改订单状态的方法
                        seckillOrderService.saveOrderFromRedisToDb(username,
                                Long.parseLong(out_trade_no),(String) map.get(
                                        "transaction_id"));
                        result = new Result(true,"支付成功！");
                    }else {
                        //调用删除订单缓存中对应订单恢复商品缓存中对应商品的方法
                        seckillOrderService.deleteOrderFromRedis(username,
                                Long.parseLong(out_trade_no));
                    }
                }
                return result;
            }
        }
    }


}
