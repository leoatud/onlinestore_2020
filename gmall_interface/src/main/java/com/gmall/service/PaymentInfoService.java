package com.gmall.service;

import com.gmall.bean.order.PaymentInfo;
import com.gmall.bean.order.PaymentStatus;

public interface PaymentInfoService {

    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo1);

    void sendPaymentToOrder(String orderId, String result);

    PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResult(String outTradeNo, Long delaySec, Integer checkCount);
}


