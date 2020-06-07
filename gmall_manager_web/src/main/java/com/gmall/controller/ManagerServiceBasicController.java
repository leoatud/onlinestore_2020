package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.base.*;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.spu.SpuImage;
import com.gmall.bean.spu.SpuInfo;
import com.gmall.bean.spu.SpuSaleAttr;
import com.gmall.serviceimpl.ManagerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManagerServiceBasicController {

    @Reference
    ManagerService managerService;


    @PostMapping("/getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1() {
        return managerService.getCatalog1();
    }

    @PostMapping("/getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id) {
        return managerService.getCatalog2(catalog1Id);
    }

    @PostMapping("/getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id) {
        return managerService.getCatalog3(catalog2Id);
    }


    @GetMapping("/attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id) {
        return managerService.getAttrInfoList(catalog3Id);
    }

    @PostMapping("/saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        managerService.saveAttrInfo(baseAttrInfo);
        return baseAttrInfo.toString();
    }

    @PostMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrInfo attrInfo = managerService.getAttrInfo(attrId);
        //相当于return attrInfo的一个属性
        return attrInfo.getAttrValueList();
    }

    /**
     * big save, 4 tables involved
     *
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        managerService.saveSpuInfo(spuInfo);
        return "success";
    }

    @PostMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return managerService.getBaseSaleAttrList();
    }

    @GetMapping("/spuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return managerService.getSpuList(catalog3Id);
    }

    /**
     * SKU start here
     */

    @GetMapping("/spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return managerService.getSpuSaleAttrList(spuId);
    }

    @GetMapping("/spuImageList")
    public List<SpuImage> getSpuImage(String spuId) {
        return managerService.getSpuImageList(spuId);
    }

}





