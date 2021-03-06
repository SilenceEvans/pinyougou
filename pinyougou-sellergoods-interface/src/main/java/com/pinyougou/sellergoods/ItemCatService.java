package com.pinyougou.sellergoods;
import com.pinyougou.pojo.TbItemCat;
import entity.PageResult;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface ItemCatService {

	/**
	 * 返回全部列表
	 * @return
	 */
    List<TbItemCat> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
    PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
    void add(TbItemCat itemCat);
	
	
	/**
	 * 修改
	 */
    void update(TbItemCat itemCat);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
    TbItemCat findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
    List<Long> delete(Long[] ids);
	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
    PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize);


	/**
	 * 根据父一级Id查询下一级内容所有信息
	 * @param parentId 父一级菜单名称
	 * @return
	 */
    List<TbItemCat> findByParentId(Long parentId);
}
