package com.pinyougou.shop.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("经过UserDetailServiceImpl实现类");

        //构建角色列表
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        //为角色列表添加角色
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //通过sellerService来查询用户是否存在且状态是否开启，若用户存在且状态开启则返回

        TbSeller seller = sellerService.findOne(username);

        String openStatus = "1";

        if (seller != null){
            //进一步判断其状态是否开启
            if (openStatus.equals(seller.getStatus())){
                    //若符合条件则返回
                return new User(username,seller.getPassword(),grantAuths);

            }else {
                return null;
            }

        }else{

            return null;
        }
    }


}
