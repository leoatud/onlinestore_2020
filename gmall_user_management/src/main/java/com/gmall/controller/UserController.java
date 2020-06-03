package com.gmall.controller;

import com.gmall.bean.UserInfo;
import com.gmall.service.UserService;
import com.gmall.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserServiceImpl userService;  //if use interface, will have no response

    @GetMapping("/users")
    public List<UserInfo> getAllUser() {
        return userService.getUserInfoListAll();
    }

    @PostMapping("/addUser")
    public String addUser(UserInfo userInfo) {
        userService.addUser(userInfo);
        return "successfully add one user: " + userInfo.toString();
    }

    @PostMapping("/updateUser")
    public String updateUser(UserInfo userInfo) {
        userService.updateUser(userInfo);
        return "successfully update one user: " + userInfo.toString();
    }

    @PostMapping("/updateUserByName")
    public String updateUserByName(UserInfo userInfo) {
        userService.updateUserByName(userInfo.getLoginName(), userInfo);
        return "successfully update one user by loginName: " + userInfo.toString();
    }

    @DeleteMapping("/deleteUser")
    public String deleteUser(UserInfo userInfo) {
        userService.deleteUser(userInfo);
        return "successfully delete one user: " + userInfo.toString();
    }

    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable String id) {
        return userService.getUserById(id).toString();
    }


}
