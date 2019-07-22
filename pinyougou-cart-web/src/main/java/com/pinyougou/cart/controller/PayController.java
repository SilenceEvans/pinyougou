package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeChatPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.OrderService;
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
    private OrderService orderService;
    @RequestMapping("/createNative")
    public Map createNative(){
        //获得登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //调用orderService中从缓存中查找支付日志信息的方法得到支付日志信息
        TbPayLog payLog = orderService.findPayLogFromRedis(username);
        if (payLog != null){
            Map nativeResult = weChatPayService.createNative(payLog.getOutTradeNo(),
                    payLog.getTotalFee()+"");
            return nativeResult;
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        int x = 0;
        while (true){
            Map map = weChatPayService.queryPayStatus(out_trade_no);
            if (map == null){
                //说明支付过程有问题
                return new Result(false,"支付失败！");
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                //如果支付成功，更新数据库中payLog的信息
                orderService.updatePayLogStatus(out_trade_no,(String)map.get(
                        "transaction_id"));
                return new Result(true,"支付成功！");
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>=10){
                return new Result(false,"支付超时");
            }
        }
    }
}
