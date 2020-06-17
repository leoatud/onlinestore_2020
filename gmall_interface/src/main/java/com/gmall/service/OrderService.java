package com.gmall.service;

import com.gmall.bean.order.OrderInfo;
import com.gmall.bean.order.ProcessStatus;

import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.Map;

public interface OrderService {

    String saveOrder(OrderInfo orderInfo);

    String genToken(String userId);

    boolean verifyToken(String userId, String token);

    void delToken(String userId);

    OrderInfo getOrderInfo(String orderId);

    void updateStatus(String orderId, ProcessStatus processStatus, OrderInfo... orderInfos);

    List<Map> orderSplit(String orderId, String wareSkuMapJson);
}
