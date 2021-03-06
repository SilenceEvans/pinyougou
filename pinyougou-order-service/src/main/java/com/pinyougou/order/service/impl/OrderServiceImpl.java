package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		String userName= order.getUserId();
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userName);
		/*
				以下部分为需要存储支付日志表信息的过程
				1.存储订单的id,new orderIdList()
				2.存储支付的总金额 double orderTotalMoney
				3.生成支付订单号并存储
				4.等等...
			 */
		List<String> orderList = new ArrayList<>();
		double orderTotalMoney = 0.00;
		for (Cart cart : cartList){
			long orderId = idWorker.nextId();
			orderList.add(orderId+"");
			//新创建订单对象
			TbOrder tborder=new TbOrder();
			//订单 ID
			tborder.setOrderId(orderId);
			//用户名
			tborder.setUserId(order.getUserId());
			//支付类型
			tborder.setPaymentType(order.getPaymentType());
			//状态：未付款
			tborder.setStatus("1");
			//订单创建日期
			tborder.setCreateTime(new Date());
			//订单更新日期
			tborder.setUpdateTime(new Date());
			//地址
			tborder.setReceiverAreaName(order.getReceiverAreaName());
			//手机号
			tborder.setReceiverMobile(order.getReceiverMobile());
			//收货人
			tborder.setReceiver(order.getReceiver());
			//订单来源
			tborder.setSourceType(order.getSourceType());
			//商家 ID
			tborder.setSellerId(cart.getSellerId());
			//循环购物车

			double money=0.00;
			for (TbOrderItem orderItem : cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());
				//订单 ID
				orderItem.setOrderId( orderId );
				orderItem.setSellerId(cart.getSellerId());
				//金额累加
				money+=orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			tborder.setPayment(new BigDecimal(money));
			orderTotalMoney += money;
			orderMapper.insert(tborder);
		}
		/*
		 以下新建支付日志实体类，封装支付日志信息
		 */
		TbPayLog payLog = new TbPayLog();
		//设置订单集合信息
		String orderListStr =
				orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
		payLog.setOrderList(orderListStr);
		//设置订单支付id
		payLog.setOutTradeNo(idWorker.nextId()+"");
		//设置总金额
		payLog.setTotalFee((long)(orderTotalMoney*100));
		//设置创建时间
		payLog.setCreateTime(new Date());
		//设置支付类型
		payLog.setPayType(order.getPaymentType());
		//刚下单，设置支付状态未支付
		payLog.setTradeState("0");
		//设置username
		payLog.setUserId(order.getUserId());
		//插入到日志表中
		payLogMapper.insert(payLog);
		//同时存入缓存
		redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		//orderMapper.insert(order);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog findPayLogFromRedis(String username) {
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
		return payLog;
	}

	@Override
	public void updatePayLogStatus(String out_trade_no, String transaction_id) {
		//从数据库中查询payLog的信息
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setTradeState("1");
		payLog.setPayTime(new Date());
		payLog.setTransactionId(transaction_id);
		//payLog表更新数据
		payLogMapper.updateByPrimaryKey(payLog);

		//订单表中更新数据
		//1.获取payLog表中orderList的值,分割字符串得到数组
		String orderList = payLog.getOrderList();
		String[] orderListStr = orderList.split(",");
		for (String orderId:orderListStr){
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (order!=null){
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			};
		}
		//所有更新完成之后清除payLog缓存
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		System.out.println("从redis中清除payLog缓存成功");
	}

}
