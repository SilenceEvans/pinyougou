package com.pinyougou.sellergoods;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 * @author wang
 */
public interface BrandService {

    /**
     * 查询所有品牌信息的方法
     * @return 品牌信息实例集合
     */
    List<TbBrand> findAll();


    /**
     * 查询所有数据分页
     * @param currentPage 当前页
     * @param Rows 每页显示的数量
     * @return 分页工具类
     */
    PageResult findAllByPage(int currentPage, int Rows);


    /**
     * 添加一条品牌信息
     * @Param name 商品名称
     * @Param firstChar 首字母
     */
    void addBrand(TbBrand tbBrand);

    /**
     * 根据id查询一条数据信息
     * @param id
     * @return
     */
    TbBrand findById(Long id);

    /**
     * 修改一条信息
     * @param tbBrand
     */
    void update(TbBrand tbBrand);


    /**
     * 根据id删除信息
     * @param ids
     */
    void delete(Long[] ids);


    /**
     * 按条件查询数据
     * @param tbBrand 查询时封装的参数
     * @param currentPage 当前页
     * @param pageSize 当前页容量
     * @return
     */
    PageResult findAllByPage(TbBrand tbBrand, int currentPage, int pageSize);


    /**
     * 品牌名称下拉列表所需要的数据
     * @return 下拉列表数据集合
     */
    List<Map> selectOptionList();
}
