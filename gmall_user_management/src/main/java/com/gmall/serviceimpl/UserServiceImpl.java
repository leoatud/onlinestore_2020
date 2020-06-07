package com.gmall.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.UserInfo;
import com.gmall.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfoList = userMapper.selectAll();
        return userInfoList;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String loginName, UserInfo userInfo) {
        //set up rules for update
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("loginName", loginName);

        userMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void deleteUser(UserInfo userInfo) {
        userMapper.deleteUser(userInfo);
    }

    @Override
    public UserInfo getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }
}
