package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //创建配置类
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //创建模板对象
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map dataModel = new HashMap();
            //根据goodsId查询sku
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            //将查询出来的spu放入数据模型
            dataModel.put("goods",goods);
            //存储商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);

            //根据goods的categoryId查询分类名称，放入数据模型
            Long category1Id = goods.getCategory1Id();
            Long category2Id = goods.getCategory2Id();
            Long category3Id = goods.getCategory3Id();
            String category1Name =
                    itemCatMapper.selectByPrimaryKey(category1Id).getName();
            String category2Name = itemCatMapper.selectByPrimaryKey(category2Id).getName();
            String category3Name = itemCatMapper.selectByPrimaryKey(category3Id).getName();
            dataModel.put("category1Name",category1Name);
            dataModel.put("category2Name",category2Name);
            dataModel.put("category3Name",category3Name);

            //根据goodsId查询对应的sku
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            //状态为有效
            criteria.andStatusEqualTo("1");
            //排序按照默认状态降序
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);
            //创建writer对象
            Writer writer = new FileWriter(pagedir+"\\"+goodsId+".html");

            template.process(dataModel,writer);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsId) {

        try {
            for (Long id : goodsId){
                new File(pagedir+"\\"+id+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
