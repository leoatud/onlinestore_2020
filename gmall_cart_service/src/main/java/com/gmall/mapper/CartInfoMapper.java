package com.gmall.mapper;

import com.gmall.bean.cart.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    List<CartInfo> selectCartListBySkuPrice(String userId);

    void mergeCartList(@Param("userIdDest") String usedIdDest, @Param("userIdOrig") String userIdOrig);

}
