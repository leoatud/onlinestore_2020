package com.gmall.service;

import com.gmall.bean.cart.CartInfo;

import java.util.List;

public interface CartService {

    CartInfo addCard(String userId, String skuId, Integer num);

    List<CartInfo> cartList(String userId);

    List<CartInfo> mergeCartList(String userId, String userIdOrig);

    void checkCart(String userId, String skuId, String isChecked);

    List<CartInfo> getCheckedCartList(String userId);
}
