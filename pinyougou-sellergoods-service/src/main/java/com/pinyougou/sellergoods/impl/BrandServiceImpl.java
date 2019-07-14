package com.pinyougou.sellergoods.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import entity.PageResult;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;


    @Override
    public List<TbBrand> findAll() {

        List<TbBrand> tbBrands = brandMapper.selectByExample(null);

        return tbBrands;
    }

    @Override
    public PageResult findAllByPage(int pageNum, int pageSize) {

        PageHelper.startPage(pageNum,pageSize);

         Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);

         PageResult pageResult = new PageResult();
         pageResult.setTotalCount(page.getTotal());
         pageResult.setTotal(page.getResult());

        return pageResult;

    }

    @Override
    public void addBrand(TbBrand tbBrand) {

        brandMapper.insert(tbBrand);

    }

    @Override
    public TbBrand findById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand tbBrand) {
        brandMapper.updateByPrimaryKey(tbBrand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids){
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findAllByPage(TbBrand tbBrand, int currentPage, int pageSize) {

        TbBrandExample example = new TbBrandExample();

        TbBrandExample.Criteria criteria = example.createCriteria();

        if (tbBrand!=null) {
            if (tbBrand.getName()!=null && tbBrand.getName().length()>0) {
                criteria.andNameLike("%"+tbBrand.getName()+"%");
            }
            if (tbBrand.getFirstChar()!=null && tbBrand.getFirstChar().length()>0) {
                criteria.andFirstCharLike("%"+tbBrand.getFirstChar()+"%");
            }
        }

        PageHelper.startPage(currentPage,pageSize);

        Page<TbBrand> brands = (Page<TbBrand>) brandMapper.selectByExample(example);

        PageResult pageResult = new PageResult();

        pageResult.setTotal(brands.getResult());

        pageResult.setTotalCount(brands.getTotal());

        return pageResult;
    }


    @Override
    public List<Map> selectOptionList() {

        List<Map> maps = brandMapper.selectOptionList();

        return maps;
    }
}
