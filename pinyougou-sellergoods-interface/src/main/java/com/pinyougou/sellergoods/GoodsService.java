package com.pinyougou.sellergoods;

import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import entity.PageResult;

import java.util.List;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
    List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
    PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
    void add(Goods goods);
	
	
	/**
	 * 修改
	 */
    void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
    Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
    void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
    PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/**
	 * 更新商品审核状态的方法
	 * @param ids 商品id
	 * @param status 页面选择通过或者驳回的状态信息
	 */
    void updateStatus(Long[] ids,String status);

	/**
	 * 商家对商品进行上下架的方法
	 * @param ids 需要进行上下架操作的商品的id
	 * @param status 上下架的标记
	 */
	void isMarketable(long[] ids, String status);

	/**
	 * @param ids 审核通过的spuId
	 * @param status 审核通过的状态
	 * @return 查询到的新审核通过的sku
	 */
    List<TbItem> findItemByGoodsIdAndStatus(Long[] ids, String status);
}
