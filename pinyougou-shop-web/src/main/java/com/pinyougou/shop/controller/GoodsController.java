package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){

		//获得商家id
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		//判断sellerId是否与商品中信息的sellerId是否一致
		//获得商家id
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//如果登录的sellerId与goods中的sellerId不同根据goods从数据库中查找到的sellerId与登录的sellerId不同则拒绝修改
		Goods goods1 = goodsService.findOne(goods.getGoods().getId());
		if (!sellerId.equals(goods1.getGoods().getSellerId())||!sellerId.equals(goods.getGoods().getSellerId())){
			return new Result(false,"非法操作！");
		}else {
			try {
				goodsService.update(goods);
				return new Result(true, "修改成功");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(false, "修改失败");
			}
		}

	}
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);
	}
	/**
	 *
	 */
	@RequestMapping("/isMarketable")
	public Result isMarketable(long[] ids,String status){
		try {
			goodsService.isMarketable(ids,status);
			//如果传过来的status为0，则代表是要下架，否则即为上架
			return "0".equals(status)?new Result(true,"下架成功！"):new Result(true,"上架成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"服务器异常，请稍后再试！");
		}
	}
	
}
