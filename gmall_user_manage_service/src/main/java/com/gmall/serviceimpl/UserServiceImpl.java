package com.gmall.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.user.UserInfo;
import com.gmall.bean.user.UserAddress;
import com.gmall.mapper.UserAddressMapper;
import com.gmall.mapper.UserMapper;
import com.gmall.service.UserService;
import com.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired //for userinfo cache useage
            RedisUtil redisUtil;

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


    String USER_KEY_PREFIX = "user:";
    String USERINFO_SUFFIX = ":info";
    int USERKEY_TIMEOUT = 60 * 60 * 24;

    /**
     * SSO logic
     *
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //check with database: md5 non-reversable
        String passwd = userInfo.getPasswd();
        String passwdMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes());

        userInfo.setPasswd(passwdMD5);
        UserInfo userInfoExist = userMapper.selectOne(userInfo);//add to cache

        if (userInfoExist != null) {
            //make the user into cache
            Jedis jedis = redisUtil.getJedis();

            String userKey = USER_KEY_PREFIX + userInfoExist.getId() + USERINFO_SUFFIX;
            jedis.setex(userKey, USERKEY_TIMEOUT, JSON.toJSONString(userInfoExist));
            jedis.close();

            return userInfoExist;
        }
        return null;
    }

    @Override
    public Boolean verify(String userId) {

        //find info in Redis and verify

        Jedis jedis = redisUtil.getJedis();
        String userKey = USER_KEY_PREFIX + userId + USERINFO_SUFFIX;
        Boolean isLogin = jedis.exists(userKey);

        if (isLogin) {
            jedis.expire(userKey, USERKEY_TIMEOUT);
        }
        jedis.close();
        return isLogin;
    }


    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }


}
