package com.gmall.serviceimpl;

import com.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name, UserInfo userInfo);

    void deleteUser(UserInfo userInfo);

    UserInfo getUserById(String id);

}
