package com.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.order.OrderDetail;
import com.gmall.bean.order.OrderInfo;
import com.gmall.bean.order.OrderStatus;
import com.gmall.bean.order.ProcessStatus;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.user.UserInfo;
import com.gmall.bean.cart.CartInfo;
import com.gmall.bean.user.UserAddress;
import com.gmall.config.LoginRequire;
import com.gmall.service.CartService;
import com.gmall.service.ManagerService;
import com.gmall.service.OrderService;
import com.gmall.service.UserService;
import com.gmall.util.HttpClientUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.internal.cglib.asm.$ClassReader;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Controller
public class OrderController {

    @Reference
    UserService userService;  // need dubbo because from different modules
    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;
    @Reference
    ManagerService managerService;

    //consumer in dubbo framework
    @GetMapping("/trade/{userId}")
    public String trades(@PathVariable String userId) {
        UserInfo userById = userService.getUserById(userId);
        return userById.toString();
    }

    @GetMapping("/trades")
    @LoginRequire
    public String trades(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");

        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);
        BigDecimal bigDecimal = new BigDecimal("0");

        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal cartInfoAmount = cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            bigDecimal = bigDecimal.add(cartInfoAmount);
        }

        String token = orderService.genToken(userId);

        request.setAttribute("tradeNo", token);
        request.setAttribute("totalAmount", bigDecimal);
        request.setAttribute("userAddressList", userAddressList);
        request.setAttribute("checkedCartList", checkedCartList);

        return "trade";
    }


    @PostMapping("/submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");

        boolean isEnableToken = orderService.verifyToken(userId, tradeNo);
        if (!isEnableToken) {
            request.setAttribute("errMsg", "page is not valid, please try again");
            return "tradeFail";
        }


        orderInfo.setUserId((String) request.getAttribute("userId"));
        //orderInfo.setUserId(orderInfo.getUserId());
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(), 15)); //过期时间15min
        orderInfo.sumTotalAmount();//自动计算订单总额

        //detailed information from skuInfo
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = managerService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName()); //因为default只有ID

            //verify price
            if (!orderDetail.getOrderPrice().equals(skuInfo.getPrice())) {
                request.setAttribute("errMsg", "price has been changed");
                return "tradeFail"; //换页面
            }
        }

        List<OrderDetail> errList = Collections.synchronizedList(new ArrayList<>());
        //check with ware stock
        Stream<CompletableFuture<String>> completableFutureStream = orderDetailList.stream().map(orderDetail -> CompletableFuture.supplyAsync(() -> checkSkuNum(orderDetail)).whenComplete((hasStock, ex) -> {
            if (hasStock.equals("0")) {
                errList.add(orderDetail);
            }
        }));

        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();

        if(errList.size()>0){
            //库存不足了
            for(OrderDetail orderDetail:errList){
                System.out.println(orderDetail.getSkuName() + " low stock");
            }
            request.setAttribute("errMesg","low stock");
            return "tradeFail";

        }

        String orderId = orderService.saveOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderid=" + orderId;
    }


    private String checkSkuNum(OrderDetail orderDetail){
        return HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
    }






    @Test
    public void test(){
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,11,12,13,14,15,16,17,18,19,20,21);
        ArrayList<Object> newList = new ArrayList<>();
        list.stream().map(num->{
            if(checkNum(num)){
                newList.add(num);
            }
            return num;
        }).count();
        System.out.println(newList);
    }

    @Test
    public void testParalle(){
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,11,12,13,14,15,16,17,18,19,20,21);
        List newList2 = new CopyOnWriteArrayList<>();
        List newList = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture[] completableFutures =
                list.stream().map(num -> CompletableFuture.supplyAsync(() -> checkNum(num)).whenComplete((ifPass, ex) -> {
            if (ifPass) {
                newList.add(num);
            }
        })).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
        System.out.println(newList);
    }


    private boolean checkNum(Integer num) {
        if (num % 3 == 0) {
            return true;
        }
        return false;
    }


    @PostMapping("/orderSplit")
    @ResponseBody
    public String orderSplit(@RequestParam("orderId") String orderId, @RequestParam("wareSkuMapJson") String wareSkuMapJson){
        List<Map> orderDetailForWareList = orderService.orderSplit(orderId, wareSkuMapJson);

        String jsonString= JSON.toJSONString(orderDetailForWareList);
        return jsonString;
    }


}
