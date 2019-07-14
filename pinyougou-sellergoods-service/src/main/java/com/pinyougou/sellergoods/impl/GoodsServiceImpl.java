package com.pinyougou.sellergoods.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper tbBrandMapper;

	@Autowired
	private TbTypeTemplateMapper templateMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;


	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	private void setItem(TbItem item,Goods goods){
		//设置商品三级分类名称
		Long category3Id = goods.getGoods().getCategory3Id();
		item.setCategoryid(category3Id);

		//设置spuId
		item.setGoodsId(goods.getGoods().getId());
		//设置商家Id
		item.setSellerId(goods.getGoods().getSellerId());
		//设置品牌名称
		TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(tbBrand.getName());
		//设置分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//设置创建时间
		item.setCreateTime(new Date());
		//设置更新时间
		item.setUpdateTime(new Date());
		//获得商家名称
		String nickName = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId()).getNickName();
		item.setSeller(nickName);
		//获得图片信息,取spu第一张照片
		String itemImages = goods.getGoodsDesc().getItemImages();
		//将获得的图片信息转换为Json,Map形式
		List<Map> maps = JSON.parseArray(itemImages, Map.class);
		//只获得其中第一条数据的信息
		//先做健壮性判断
		if (maps.size()>0){
			String url = (String)maps.get(0).get("url");
			item.setImage(url);
		}
	}

	public void saveItem(Goods goods){
		//如果开启规格，则遍历添加item，否则只添加单条
		if ("1".equals(goods.getGoods().getIsEnableSpec())){
			//获取ItemList值，并进行遍历
			for (TbItem item : goods.getItemList()){
				//在往表中插入数据之前，先获得item没有的数据
				//设置商品title spu+item表中的spec
				String spu = goods.getGoods().getGoodsName();
				String title = spu + item.getSpec();
				item.setTitle(title);
				this.setItem(item,goods);
				//往item表中插入前台获得得item的数据
				itemMapper.insert(item);
			}
		}else{
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			this.setItem(item,goods);
			itemMapper.insert(item);
		}
	}
	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {

		//往商品表中添加信息之前，设置其状态为0
		goods.getGoods().setAuditStatus("0");
		//往商品表中插入商品信息
		goodsMapper.insert(goods.getGoods());
		//获取插入之后商品的id
		Long id = goods.getGoods().getId();
		//往商品描述表中添加相关信息
		goods.getGoodsDesc().setGoodsId(id);
		goodsDescMapper.insert(goods.getGoodsDesc());
		//调用判断保存的方法
		saveItem(goods);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//先删除再添加(因为无法确定item条目修改后的数量是不是与修改前一样，如果新增了一些条目，那么在选择update方法时是在数据库中找不到这些数据的)
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//调用保存的方法
		saveItem(goods);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(goodsDesc);
		//根据goodsId查询item
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 * 对商品的删除应是逻辑删除，即对删除的数据在数据库中做一定的标记，但不从数据库中真正删掉
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//首先查询到该商品
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			//设置查询到的信息中的isDelete值为1，并将该信息在数据库中进行更新
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
               criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		//对商品编号进行遍历，根据编号查出对应的每条商品信息，每条商品信息再更新其状态的值，更新完成后再调用updateByPrimaryKey
		// 方法更新数据库中每条商品信息
		for (Long id :ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	@Override
	public void isMarketable(long[] ids, String status) {
		//循环拿到的每一条商品的id，查找每一条商品信息
		//设置每一条商品信息的isMarketable为相应的status
		//调用更新方法，将设置好status的信息更新到数据库中
		for (Long id: ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsMarketable(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	@Override
	public List<TbItem> findItemByGoodsIdAndStatus(Long[] ids, String status) {

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);
		criteria.andGoodsIdIn(Arrays.asList(ids));
		List<TbItem> items = itemMapper.selectByExample(example);
		return items;
	}

}
