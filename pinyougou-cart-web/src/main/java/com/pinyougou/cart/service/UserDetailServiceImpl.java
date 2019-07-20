package com.pinyougou.cart.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("经过UserDetailServiceImpl实现类");

        //构建角色列表
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        //为角色列表添加角色
        grantAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username,"",grantAuths);

    }
}
