package com.gmall.mapper.base;

import com.gmall.bean.base.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    //link to mapper.xml
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    //search result by multiple attribute id, 类似于filter
    List<BaseAttrInfo> getBaseAttrInfoListByValueIds(@Param("valueIds") String valueIds);
}
