package com.gmall.service;

import com.gmall.bean.user.UserInfo;
import com.gmall.bean.user.UserAddress;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name, UserInfo userInfo);

    void deleteUser(UserInfo userInfo);

    UserInfo getUserById(String id);

    UserInfo login(UserInfo userInfo);

    Boolean verify(String userId);

    List<UserAddress> getUserAddressList(String userId);
}
