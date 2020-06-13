package com.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.spu.SpuSaleAttr;
import com.gmall.config.LoginRequire;
import com.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    ManagerService managerService;

    @GetMapping("/{skuId}.html")  //伪静态的html path
    //@LoginRequire(autoRedirect = true)
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request) {

        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList = managerService.getSpuSaleAttrListBySpuIdCheckSku(skuInfo.getSpuId(), skuId);
        //String jsonString = JSON.toJSONString(skuInfo);

        //cannot be empty
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("spuSaleAttrList", spuSaleAttrList);

        //get group_concat, complex sql
        Map skuValueIdsMap = managerService.getSkuValueIdsMap(skuInfo.getSpuId());
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("skuValueIdsMap", valuesSkuJson);


        return "item";
    }


}
