package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import entity.PageResult;
import entity.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {


    @Reference
    private BrandService brandService;


    @RequestMapping("/findAllBrands.do")
    public List<TbBrand> findAllBrands(){

        List<TbBrand> brands = brandService.findAll();

        return brands;

    }


    @RequestMapping("/findAllByPage.do")
    public PageResult findAlByPage(int pageNum, int pageSize){

        return brandService.findAllByPage(pageNum,pageSize);

    }

    /**
     * 新增一条商品信息
     * @param tbBrand
     * @return
     */
    @RequestMapping("/addBrand.do")
    public Result addBrand(@RequestBody TbBrand tbBrand){

        try {
            brandService.addBrand(tbBrand);
            return new Result(true,"添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }

    /**
     * 修改时先通过id进行数据的查询，将查询出的数据回写到要修改的表单中
     * @param id
     * @return
     */
    @RequestMapping("/findById.do")
    public TbBrand findById(Long id){

       TbBrand tbBrand = brandService.findById(id);

       return tbBrand;
    }


    /**
     * 修改一条信息
     * @param tbBrand
     * @return
     */

    @RequestMapping("/update.do")
    public Result update(@RequestBody TbBrand tbBrand){

        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"修改失败");
        }
    }


    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){

        try {
            brandService.delete(ids);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"修改失败");
        }
    }


    @RequestMapping("/search.do")

    public PageResult findByCondition(@RequestBody TbBrand tbBrand, int currentPage,
                                      int pageSize){

        PageResult allByPage = brandService.findAllByPage(tbBrand, currentPage, pageSize);
        return allByPage;

    }


    @RequestMapping("/selectOptionList.do")

    public List<Map> selectOptionList(){

        List<Map> maps = brandService.selectOptionList();

        return maps;
    }

}
