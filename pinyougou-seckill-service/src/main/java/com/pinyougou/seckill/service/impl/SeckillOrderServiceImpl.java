package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.itheima.seckill.service.SeckillOrderService;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}


	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}

		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public Result submitOrder(Long seckillGoodId, String username) {
		//先存缓存中查询该用户提交的这个订单是否还存在
		TbSeckillGoods seckillgood = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillGoodId);
		if (seckillgood == null) {
			return new Result(false, "该商品已经卖完了！");
		}
		if (seckillgood.getStockCount() <= 0) {
			return new Result(false, "该商品已经卖完");
		}
		//如果上述条件不满足，则说明缓存中该商品可卖,每执行一次,库存减一，同时将订单信息放入缓存

		if (seckillgood.getStockCount()-1 == 0){
			//清除该商品缓存
			redisTemplate.boundHashOps("seckillGoods").delete(seckillGoodId);
			System.out.println("删除该秒杀商品的缓存");
			//更新到数据库
			seckillgood.setStockCount(0);
			seckillGoodsMapper.updateByPrimaryKey(seckillgood);
		}else {
			//更新缓存中该商品的信息
			seckillgood.setStockCount(seckillgood.getStockCount() - 1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoodId,seckillgood);
			System.out.println("秒杀成功，更新缓存中该商品的信息");
		}
		//设置TbSecOrder实体属性
		long seckillOrderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(seckillOrderId);
		seckillOrder.setSeckillId(seckillGoodId);
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillgood.getCostPrice());
		seckillOrder.setSellerId(seckillgood.getSellerId());
		seckillOrder.setUserId(username);
		seckillOrder.setStatus("0");
		//将生成的订单信息放入缓存
		redisTemplate.boundHashOps("seckillOrders").put(username,seckillOrder);
		return new Result(true,"订单提交成功！");
	}

	/**
	 * 查找缓存中是否存在该用户名对应的商品
	 * @param username 用户名
	 */
	@Override
	public TbSeckillOrder findOrderFromRedisByUsername(String username) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(username);
		return seckillOrder;
	}

	/**
	 * 支付成功后将订单保存至数据库
	 * @param username 用户名
	 * @param orderId 订单id
	 * @param transactionId 交易单号
	 */
	@Override
	public void saveOrderFromRedisToDb(String username, Long orderId, String transactionId) {
		//根据usernam查询缓存中是否存在该订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(username);
		if (seckillOrder == null){
			throw new RuntimeException("要保存的订单不存在");
		}
		if (orderId.longValue() != seckillOrder.getId()){
			throw new RuntimeException("订单不相符");
		}
		seckillOrder.setPayTime(new Date());
		seckillOrder.setTransactionId(transactionId);
		//更改订单状态为已支付
		seckillOrder.setStatus("1");
		//保存订单至数据库
		seckillOrderMapper.insert(seckillOrder);
		//删除缓存中该订单的相关信息
		redisTemplate.boundHashOps("seckillOrders").delete(username);
	}

	/**
	 * 当超时未支付时删除订单缓存中的对应订单，并恢复商品缓存中的对应商品
	 * @param username 用户名
	 * @param orderId  订单编号
	 */
	@Override
	public void deleteOrderFromRedis(String username, Long orderId) {
		//查找订单缓存中该订单是否存在
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(username);
		if (seckillOrder!=null&&seckillOrder.getId().longValue()==orderId){
			//删除该订单
			redisTemplate.boundHashOps("seckillOrders").delete(username);
			//恢复商品缓存中的商品
			Long seckillId = seckillOrder.getSeckillId();
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			if (seckillGoods==null){
				//将库存数量重新设置为1
				TbSeckillGoods goods = seckillGoodsMapper.selectByPrimaryKey(seckillId);
				goods.setStockCount(1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillId,goods);
			}else {
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				//将恢复后的商品信息放入商品缓存
				redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
			}
		}
	}
}
