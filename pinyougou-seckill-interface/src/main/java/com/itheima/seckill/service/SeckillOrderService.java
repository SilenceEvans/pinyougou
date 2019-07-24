package com.itheima.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
import entity.Result;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 秒杀商品提交订单的方法
	 * @param seckillGoodId 秒杀商品的id
	 * @param username 用户名
	 * @return 提交订单的结果
	 */
	public Result submitOrder(Long seckillGoodId, String username);

	/**
	 * 根据用户名查询缓存中是否存在该用户名对应的订单
	 * @param username 用户名
	 * @return 该用户秒杀的订单
	 */
	TbSeckillOrder findOrderFromRedisByUsername(String username);

	/**
	 * 支付成功之后调用该方法将订单保存至数据库
	 * @param username 用户名
	 * @param orderId 订单id
	 * @param transactionId 交易单号
	 */
	void saveOrderFromRedisToDb(String username,Long orderId,String transactionId);


	/**
	 * 当超时未支付时删除订单缓存中的对应订单，并恢复商品缓存中的对应商品
	 * @param username 用户名
	 * @param orderId 订单编号
	 */
	void deleteOrderFromRedis(String username,Long orderId);
}
