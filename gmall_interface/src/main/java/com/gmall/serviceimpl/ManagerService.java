package com.gmall.serviceimpl;

import com.gmall.bean.base.*;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.spu.SpuImage;
import com.gmall.bean.spu.SpuInfo;
import com.gmall.bean.spu.SpuSaleAttr;

import java.util.List;
import java.util.Map;

public interface ManagerService {


    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);


    //for attr, for detailed item
    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    BaseAttrInfo getAttrInfo(String attrId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);

    List<BaseSaleAttr> getBaseSaleAttrList();

    //big save, spu info: click save to trigger big save
    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuInfo> getSpuList(String catalog3Id);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //sku starter here
    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuIdCheckSku(String spuId, String skuId);

    //get group_concat skuAttrIdsMap
    Map getSkuValueIdsMap(String spuId);


}
