package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
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

	@Autowired
	private Destination queueSolrDestination;

	@Autowired
	private Destination queueDeleteSolrDestination;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination topicPageDestination;

	@Autowired
	private Destination topicDeletePageDestination;


	
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

	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){

		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
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
	public Result delete(final Long [] ids){
		try {
			if (ids != null && ids.length > 0){
				goodsService.delete(ids);
				jmsTemplate.send(queueDeleteSolrDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
				//删除对应的静态页面
				jmsTemplate.send(topicDeletePageDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
				return new Result(true, "删除成功!");
			}else {
				return new Result(false, "所删除的内容不能为空!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败!");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page 当前页
	 * @param rows 容量
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			if ("1".equals(status)){
				//调用goodsService实现类中findItemByGoodsIdAndStatus方法查询哪些商品添加到了item表中
				List<TbItem> items =goodsService.findItemByGoodsIdAndStatus(ids,status);

				//将items发送到消息中间件
				if (items.size()>0){
					final String jsonString = JSON.toJSONString(items);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
				}else {
					System.out.println("无可用数据上传至solr索引库");
				}

				//审核完成之后生成静态网页
				for (final Long id : ids){
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+"");
						}
					});
				}
				return new Result(true,"此商品已审核通过！");
			}else {
				return new Result(true,"此商品审核不予通过！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"审核异常");
		}
	}

}
