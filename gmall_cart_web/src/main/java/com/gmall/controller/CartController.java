package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.cart.CartInfo;
import com.gmall.config.LoginRequire;
import com.gmall.service.CartService;
import com.gmall.util.CookieUtil;
import org.assertj.core.internal.cglib.asm.$ClassReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @PostMapping("/addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(@RequestParam("skuId") String skuId,
                            @RequestParam("num") int num,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userId != null) {
            } else {
                userId = UUID.randomUUID().toString();
                //还要存一份cookie在浏览器中
                CookieUtil.setCookie(request, response, "user_temp_id", userId, 60 * 60 * 24 * 7, false);
            }
        }
//
//        if (userId != null) {
//            //first time to access, new everything
//            cartService.addCard(userId, skuId, num);
//        } else {
//            //check if user has token, if yes, use token as id to add cart,
//            //if not, generate new token and add to cache
//            String user_tmp_id = CookieUtil.getCookieValue(request, "user_tmp_id", false);
//            if(user_tmp_id!=null){
//                //do the same thing as before
//                cartService.addCard(user_tmp_id,skuId, num);
//            }else{
//                //things we need to handle
//                user_tmp_id = UUID.randomUUID().toString();
//                cartService.addCard(user_tmp_id,skuId, num);
//            }
//        }
        CartInfo cartInfo = cartService.addCard(userId, skuId, num);
        request.setAttribute("cartInfo", cartInfo);
        return "success";
    }

    //查询购物列表
    @GetMapping("/cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request) {
        //check if login or not
        String userId = (String) request.getAttribute("userId");
        //user login case start here ==========================
        if (userId != null) {
            List<CartInfo> cartList = null;  //merge if has temp id
            String userTempId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userTempId != null) {
                //if has temp id, then get temp cart
                List<CartInfo> cartTempList = cartService.cartList(userTempId);
                //just merge if has, no matter if you login or not
                if (userId != null && cartTempList != null && cartTempList.size() > 0) {
                    //cartlist update here
                    cartList = cartService.mergeCartList(userId, userTempId);
                }
            }

            if (cartList == null || cartList.size() == 0) {
                //if no temp id, then just use login cart
                cartList = cartService.cartList(userId);
            }
            request.setAttribute("cartList", cartList);
        } else {
            //user not login case start here ==========================
            //only care temp cart
            String userTempId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userTempId != null) {
                List<CartInfo> cartTempList = cartService.cartList(userTempId);
                request.setAttribute("cartList", cartTempList);
            }
        }
        return "cartList";
    }

    @PostMapping("/checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public String checkCart(@RequestParam("isChecked")String isChecked,
                            @RequestParam("skuId") String skuId,
                            HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        if(userId==null){
            userId = CookieUtil.getCookieValue(request,"user_temp_id",false);
        }
        cartService.checkCart(userId,skuId, isChecked); // ischecked is the check box
        return "success";
    }
}
