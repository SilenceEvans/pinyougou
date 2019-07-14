package com.pinyougou.sellergoods.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.TypeTemplateService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

		//每次查询完后调用redis中存储规格和模板信息的方法
		redisSaveSpecAndBrand();
			System.out.println("每次查询完后调用redis中存储规格和模板信息的方法");
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionTemplateList() {
		return typeTemplateMapper.selectOptionTemplateList();
	}

	@Override
	public Map selectOptionTemplateName(Long id) {

		return typeTemplateMapper.selectOptionTemplateName(id);

	}


	@Override
	public List<Map> findSpecList(Long id) {

		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//从以下开始变量名有误，brandIds实际是specIds
		String brandIds = typeTemplate.getSpecIds();

		//解析brandIds字符串为json集合
		List<Map> brandIdsMaps= JSON.parseArray(brandIds, Map.class);

		for (Map brandIdsMap : brandIdsMaps){

			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(new Long((Integer)brandIdsMap.get("id")));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);

			brandIdsMap.put("options",options);
		}

		return brandIdsMaps;
	}


	private void redisSaveSpecAndBrand(){
		//查询所有的模板信息
		List<TbTypeTemplate> templates = findAll();
		for (TbTypeTemplate template : templates){
			String brandIds = template.getBrandIds();
			//将获得的品牌信息的字符串转为map集合
			List<Map> brandList = JSON.parseArray(brandIds, Map.class);
			redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);

			//调用findSpecList方法，将模板id与规格信息存入
			List<Map> specList = findSpecList(template.getId());
			redisTemplate.boundHashOps("specList").put(template.getId(),specList);
		}
	}


}
