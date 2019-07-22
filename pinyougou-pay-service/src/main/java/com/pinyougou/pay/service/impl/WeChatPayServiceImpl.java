package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatPayServiceImpl implements WeChatPayService {
   //公众账号
    @Value("${appid}")
    private String appid;
    //商户号
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建参数
        Map<String,String> param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put ("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","品优购商品结算");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://baidu.com");
        param.put("trade_type","NATIVE");

        //生成要发送的xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
           //是否是http协议
            client.setHttps(true);
            //发送的xml数据
            client.setXmlParam(xmlParam);
            //执行post请求
            client.post();
            //获取结果
            String content = client.getContent();
            System.out.println(content);
            //将结果转为集合
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            Map<String,String> map = new HashMap<>();
            //付款地址
            map.put("code_url",xmlToMap.get("code_url"));
            //总金额
            map.put("total_fee",total_fee);
            //商户订单号
            map.put("out_trade_no",out_trade_no);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> param = new HashMap();
        //公众号
        param.put("appid",appid);
        //商户号
        param.put("mch_id",partner);
        //订单号
        param.put("out_trade_no",out_trade_no);
        //随机字符串
        param.put ("nonce_str", WXPayUtil.generateNonceStr());
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        try {
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            client.setHttps(true);
            client.setXmlParam(signedXml);
            client.post();
            String content = client.getContent();
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            System.out.println(xmlToMap);
            return xmlToMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}
