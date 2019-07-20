package com.pinyougou.cart.controller;
import java.util.List;

import com.pinyougou.userService.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;



/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回指定用户地址信息
	 * @return
	 */
	@RequestMapping("/findUserAddress")
	public List<TbAddress> findUserAddressByUsername(){
		//获取用户名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		List<TbAddress> userAddressList = userService.findUserAddressByUsername(username);
		return userAddressList;
	}
	
}
