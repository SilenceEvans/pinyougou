package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//增加时需要删除缓存重新从数据库查找，从content对象中得到要增加的商品的categoryId
		//删除categoryId对应的缓存
		contentMapper.insert(content);
		Long categoryId = content.getCategoryId();
		redisTemplate.boundHashOps("content").delete(categoryId);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		/*
		更改时有两种情况：
		第一种：不更改categoryId，其余内容更改，则此时只需要删除该categoryId对应的缓存
		第二种：更改categoryId，则此时更改前categoryId对应的缓存与更改后categoryId对应的缓存都应该删除，重新加载
		 */
		//先根据id查询数据库中该id对应商品信息的categoryId
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//删除该categoryId对应的缓存
		redisTemplate.boundHashOps("content").delete(categoryId);
		contentMapper.updateByPrimaryKey(content);
		//对从数据库中得到的categoryId和content对象中的categoryId进行比较
		if (categoryId.longValue()!=content.getCategoryId().longValue()){
			//如果不等于成立，则删除新categoryId对应的缓存
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//删除时也需要更新缓存，每删除一个id对应的广告信息，则更新其categoryId对应的缓存信息
		for(Long id:ids){
			//根据id查找商品信息，得到其对应的categoryId
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			Long categoryId = tbContent.getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findContentsByCategoryId(Long categoryId) {

		//先判断缓存中是否存在，如果从在则从缓存中拿取
		List<TbContent> tbContents = (List<TbContent>) redisTemplate.boundHashOps(
				"content").get(categoryId);
		if (tbContents == null){
			//若果缓存中查找的值为null，则说明缓存中没有，这时需要从数据库中查找,查找完成后，将其放入缓存
			//根据其广告类型id与广告是否可用状态进行条件查找
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("sort_order");
			List<TbContent> contents = contentMapper.selectByExample(example);
			redisTemplate.boundHashOps("content").put(categoryId,contents);
			return contents;
		}else{
			return tbContents;
		}

	}

}
