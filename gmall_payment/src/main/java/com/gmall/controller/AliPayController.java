package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.gmall.bean.order.OrderInfo;
import com.gmall.bean.order.PaymentInfo;
import com.gmall.bean.order.PaymentStatus;
import com.gmall.config.AlipayConfig;
import com.gmall.service.OrderService;
import com.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Controller
@CrossOrigin
public class AliPayController {

    @Reference
    PaymentInfoService paymentInfoService;
    @Reference
    OrderService orderService;
    @Autowired
    AlipayClient alipayClient;


    //redirect from orderController: return "redirect://payment.gmall.com/index?orderid=" + orderInfo.getId();
    @GetMapping("/index")
    public String index(String orderId, HttpServletRequest request) {

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @PostMapping("/alipay/submit")
    @ResponseBody
    public String submit(String orderId, HttpServletResponse response) {

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_order_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        long currentmillisecond = System.currentTimeMillis();
        String outTradeNo = orderId + currentmillisecond;
        String productNo = "FAST_INSTANT_TRADE_PAY";
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        String subject = orderInfo.genSubject();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("product_code", productNo);
        jsonObject.put("total_amount", totalAmount);
        jsonObject.put("subject", subject);
        alipayRequest.setBizContent(jsonObject.toJSONString());
        String pageHtml = "";
        try {
            pageHtml = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        //backup a copy of alipay info
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(subject);
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        paymentInfoService.savePaymentInfo(paymentInfo);
        paymentInfoService.sendDelayPaymentResult(outTradeNo, 10L, 3);

        return pageHtml;
    }


    @PostMapping("/alipay/callback/notify")
    public String notify(HttpServletRequest request, @RequestParam Map<String, String> paramMap) throws AlipayApiException {
        //verify signature
        String sign = (String) paramMap.get("sign");
        boolean ifPass = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        if (ifPass) {
            String out_trade_no = paramMap.get("out_trade_no");
            String total_amount = paramMap.get("total_amount");
            String trade_status = paramMap.get("trade_status");

            if ("TRADE_SUCCESS".equals(trade_status)) {

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo1 = paymentInfoService.getPaymentInfo(paymentInfo);
                if (paymentInfo1.getTotalAmount().compareTo(new BigDecimal(total_amount)) == 0) {

                    PaymentInfo updateInfo = new PaymentInfo();
                    updateInfo.setPaymentStatus(PaymentStatus.PAID);
                    updateInfo.setCallbackTime(new Date());
                    updateInfo.setCallbackContent(JSON.toJSONString(paramMap));
                    updateInfo.setAlipayTradeNo(paramMap.get("trade_no"));

                    paymentInfoService.updatePaymentInfoByOutTradeNo(out_trade_no, paymentInfo1);

                    return "success";
                } else if (paymentInfo1.getPaymentStatus().equals(PaymentStatus.ClOSED)) {
                    return "fail";
                } else if (paymentInfo1.getPaymentStatus().equals(PaymentStatus.PAID)) {
                    return "success";
                }

            }
        }
        return "fail";
    }

    @ResponseBody
    @PostMapping("/alipay/callback/return")
    public String alipayReturn(){
        return "success";
    }


    @GetMapping("/refund")
    @ResponseBody
    public String refund(String orderId){
        return "success";
    }

    @GetMapping("/sendPayment")
    public String sendPayment(String orderId){
        paymentInfoService.sendPaymentToOrder(orderId,"success");
        return "success";
    }


}
