package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.wxpay.sdk.WXPayUtil;
import com.gmall.bean.order.OrderInfo;
import com.gmall.service.OrderService;
import com.gmall.util.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@PropertySource("classpath:wxpay.properties")
public class WxPayController {

    @Reference
    OrderService orderService;

    @Value("${appid}")
    String appid;
    @Value("${partner}")
    String partner;
    @Value("${partnerkey}")
    String partnerkey;

    @PostMapping("wx/submit")
    public String wxSubmit(String orderId) throws Exception {
        //return code_url
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        Map<String, String> param = new HashMap();
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee",orderInfo.getTotalAmount().multiply(new BigDecimal(100)).toBigInteger().toString());//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://order.gmall.com/trade");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型

        String xmlParam  = WXPayUtil.generateSignedXml(param, partnerkey);

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setXmlParam(xmlParam);
        httpClient.post();
        String content = httpClient.getContent(); // xml content becasue wechat is old way

        Map<String, String> returnMap = WXPayUtil.xmlToMap(content);
        if(returnMap.containsKey("code_url")){
            String code_url = returnMap.get("code_url");
            return code_url;
        }else{
            System.out.println(returnMap.get("return_code"));
            System.out.println(returnMap.get("return_msg"));
            return null;
        }
    }


    @PostMapping("/wx/callback/notify")
    public String notify(HttpServletRequest request) throws IOException {

        return "10";

    }


}
