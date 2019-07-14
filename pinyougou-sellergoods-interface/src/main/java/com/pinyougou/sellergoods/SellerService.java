package com.pinyougou.sellergoods;

import com.pinyougou.pojo.TbSeller;
import entity.PageResult;

import java.util.List;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SellerService {

	/**
	 * 返回全部列表
	 * @return
	 */
    List<TbSeller> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
    PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
    void add(TbSeller seller);
	
	
	/**
	 * 修改
	 */
    void update(TbSeller seller);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
    TbSeller findOne(String id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
    void delete(String[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
    PageResult findPage(TbSeller seller, int pageNum, int pageSize);


	/**
	 * 根据sellerId值修改商家状态
	 * @param sellerId
	 * @param status
	 */
    void updateStatus(String sellerId, String status);
	
}