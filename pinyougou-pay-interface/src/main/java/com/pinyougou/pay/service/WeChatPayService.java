package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 实现微信付款的接口
 */
public interface WeChatPayService {

    /**
     * 创建二维码的方法
     * @param out_trade_no 商户订单号
     * @param total_fee 标价金额
     * @return xml文件存储在map集合中
     */
     Map createNative(String out_trade_no,String total_fee);

    /**
     * 查询订单状态的方法
     * @param out_trade_no 订单号
     * @return map集合
     */
     Map queryPayStatus(String out_trade_no);

    /**
     * 关闭订单的方法
     * @param out_trade_no 商品下单的单号
     * @return map集合
     */
     Map closePay(String out_trade_no);
}
