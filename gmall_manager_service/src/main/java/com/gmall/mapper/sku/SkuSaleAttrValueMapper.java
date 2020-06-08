package com.gmall.mapper.sku;

import com.gmall.bean.sku.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    //hashmap contain information we need
    List<Map> getSaleAttrValuesBySpu(String spuId);
}
