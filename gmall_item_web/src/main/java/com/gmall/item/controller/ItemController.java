package com.gmall.item.controller;

import com.alibaba.fastjson.JSON;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.serviceimpl.ManagerService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    @Reference
    ManagerService managerService;

    @GetMapping("/{skuId}.html")  //伪静态的html path
    public String item(@PathVariable("skuId") String skuId){
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        String jsonString = JSON.toJSONString(skuInfo);
        return jsonString;
    }
}
