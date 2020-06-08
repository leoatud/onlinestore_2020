package com.gmall.mapper.spu;

import com.gmall.bean.spu.SpuSaleAttr;
import com.gmall.bean.spu.SpuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuIdCheckSku(@Param("spuId") String spuId, @Param("skuId") String skuId);
}
