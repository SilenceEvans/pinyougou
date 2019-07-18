package com.pinyougou.user.controller;
import java.util.List;

import com.pinyougou.userService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.userService.UserService;

import entity.PageResult;
import entity.Result;
import util.PhoneFormatCheckUtils;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		if(!userService.checkSmsCode(user.getPhone(),code)){
			return new Result(false,"验证码输入错误");
		}
		try {
			userService.add(user);
			return new Result(true, "注册成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "注册失败！");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
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
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
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
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);		
	}

	/**
	 * 注册时发送短信验证码
	 * @param phone 注册时提交的手机号
	 * @return 发送短信验证码的结果
	 */
	@RequestMapping("/sendSmsCode.do")
	public Result sendSmsCode(String phone){
		//为保证程序的健壮性，对mobile进行非空和正则校验
		if (phone == null || "".equals(phone)){
			return new Result(false,"请输入手机号！");
		}else if (!PhoneFormatCheckUtils.isChinaPhoneLegal(phone)){
			return new Result(false,"请输入正确格式的手机号！");
		}else{
			try {
				userService.sendSmsCode(phone);
				return new Result(true,"短信发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(true,"短信发送失败！");
			}
		}
	}
}
