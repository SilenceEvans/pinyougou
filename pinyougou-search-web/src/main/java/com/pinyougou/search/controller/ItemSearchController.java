package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/itemSearch")
@RestController
public class ItemSearchController {

    @Reference
    private ItemSearchService searchService;
    @RequestMapping("/search")
    public Map itemSearch(@RequestBody Map searchMap){
        Map searchResult = searchService.search(searchMap);
        return searchResult;
    }
}
