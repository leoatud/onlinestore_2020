package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.service.ManagerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MangerServiceSkuController {

    @Reference
    ManagerService managerService;


    @PostMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        managerService.saveSkuInfo(skuInfo);
        return "success";
    }
}
