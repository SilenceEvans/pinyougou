package com.pinyougou.sellergoods.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.SpecificationService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//首先插入规格名称
		specificationMapper.insert(specification.getSpecification());


		//其次插入插入规格的选项
		for (TbSpecificationOption tbSpecificationOption:specification.getSpecificationOptionList()){

			tbSpecificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(tbSpecificationOption);

		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		//先修改tbSpecification中的内容

		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);

		//再修改tbSpecificationOption中的内容，先删除，后添加
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(tbSpecification.getId());
		specificationOptionMapper.deleteByExample(example);

		//再次添加
		for(TbSpecificationOption tbSpecificationOption:
				specification.getSpecificationOptionList()){
			tbSpecificationOption.setSpecId(tbSpecification.getId());
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){

		Specification specification = new Specification();
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

		//specification设置属性specification
		specification.setSpecification(tbSpecification);

		//根据specification的id即specificationOption的specId查询specificationOption


		TbSpecificationOptionExample example = new TbSpecificationOptionExample();

		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();

		criteria.andSpecIdEqualTo(id);

		List<TbSpecificationOption> tbSpecificationOptionList = specificationOptionMapper.selectByExample(example);

		//specification设置属性List<TbSpecification>
		specification.setSpecificationOptionList(tbSpecificationOptionList);

		return specification;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        for(Long id:ids){
            criteria.andSpecIdEqualTo(id);
            specificationMapper.deleteByPrimaryKey(id);
            specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 查询规格下拉列表数据
	 * @return
	 */
	@Override
	public List<Map> selectOptionSpecificationList() {
		return specificationMapper.selectOptionSpecificationList();
	}

}
