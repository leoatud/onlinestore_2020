package com.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.UserInfo;
import com.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Reference
    UserService userService;  // need dubbo because from different modules

    //consumer in dubbo framework
    @GetMapping("/trade/{userId}")
    public String trades(@PathVariable String userId) {
        UserInfo userById = userService.getUserById(userId);
        return userById.toString();
    }

    @GetMapping("/trades")
    public String trades() {
        return userService.getUserInfoListAll().toString();
    }


}
