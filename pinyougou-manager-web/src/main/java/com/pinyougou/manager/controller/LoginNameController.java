package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/index")
public class LoginNameController {

    @RequestMapping("/loginName.do")
    public Map getLoginName(){

        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();

        Map loginNameMap = new HashMap();

        loginNameMap.put("loginName",loginName);

        return loginNameMap;

    }

}
