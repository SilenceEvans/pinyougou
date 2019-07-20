package com.pinyougou.userService;
import java.util.List;

import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbUser;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface UserService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbUser> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbUser user);
	
	
	/**
	 * 修改
	 */
	public void update(TbUser user);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbUser findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbUser user, int pageNum, int pageSize);

	/**
	 * 新用户进行注册时发送短信
	 * @param mobile 要发送短信的手机号码
	 */
    void sendSmsCode(String mobile);

	/**
	 * @param phone 添加时实体里的电话号码
	 * @return 缓存中是否存在该电话号码对应的验证码
	 */
    Boolean checkSmsCode(String phone,String code);


	/**
	 * 根据用户名查找用户所拥有的地址信息
	 * @param username 用户名
	 * @return 用户地址信息集合
	 */
    List<TbAddress> findUserAddressByUsername(String username);
}
