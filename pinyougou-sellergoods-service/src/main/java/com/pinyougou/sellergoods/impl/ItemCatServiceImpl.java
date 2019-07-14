package com.pinyougou.sellergoods.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.ItemCatService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private RedisTemplate redisTemplate;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public List<Long> delete(Long[] ids) {


		List<Long> haveChild = new ArrayList<>();
		for(Long id:ids){
			if (this.findByParentId(id)!=null && this.findByParentId(id).size() > 0){
				haveChild.add(id);
			} else{
				itemCatMapper.deleteByPrimaryKey(id);
			}
		}

		return haveChild;

	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 查询下一级内容所有信息
	 * @param parentId 父一级菜单名称
	 * @return
	 */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {

		TbItemCatExample example = new TbItemCatExample();

		Criteria criteria = example.createCriteria();

		criteria.andParentIdEqualTo(parentId);

		List<TbItemCat> ItemCats = itemCatMapper.selectByExample(example);
		//每次查询出分类集合之后，遍历集合，将每一个元素的分类名及模板id放到缓存中
		List<TbItemCat> itemCatsList = findAll();
		for (TbItemCat itemCat : itemCatsList){
			redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
		}
		System.out.println("将分类名和模板Id放入缓存");
		return ItemCats;
	}

}
