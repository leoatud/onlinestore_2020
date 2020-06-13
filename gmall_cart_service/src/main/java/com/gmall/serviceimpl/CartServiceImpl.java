package com.gmall.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gmall.bean.cart.CartInfo;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.mapper.CartInfoMapper;
import com.gmall.service.CartService;
import com.gmall.service.ManagerService;
import com.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ManagerService managerService;

    public CartInfo addCard(String userId, String skuId, Integer num) {

        //database--> mysql
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);

        CartInfo cartInfoEx = cartInfoMapper.selectOne(cartInfo);
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        if (cartInfoEx != null) {
            cartInfoEx.setSkuName(skuInfo.getSkuName());
            cartInfoEx.setCartPrice(skuInfo.getPrice());
            cartInfoEx.setSkuNum(cartInfoEx.getSkuNum() + num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            //already exist
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoEx);
        } else {
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(num);
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(cartInfoEx.getSkuNum() + num);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            //new
            cartInfoEx = cartInfo1;
            cartInfoMapper.insertSelective(cartInfo1);
        }

        //cache--> redis
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:" + userId + ":info";
        String cartInfoJson = JSON.toJSONString(cartInfoEx);
        jedis.hset(cartKey, skuId, cartInfoJson);
        jedis.close();

        return cartInfoEx;
    }

    public List<CartInfo> cartList(String userId) {
        ArrayList<CartInfo> cartInfos = new ArrayList<CartInfo>();

        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:" + userId + ":info";
        List<String> cartJsonList = jedis.hvals(cartKey);
        if (cartJsonList != null && cartJsonList.size() > 0) {
            //check redis first

            for (String s : cartJsonList) {
                cartInfos.add(JSON.parseObject(s, CartInfo.class));
            }

            cartInfos.sort(new Comparator<CartInfo>() {
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            //cache found, return
            return cartInfos;
        } else {
            //check database
            return loadCartCache(userId);
        }
    }


    @Override
    public List<CartInfo> mergeCartList(String userId, String userIdOrig) {
        //先做合并
        cartInfoMapper.mergeCartList(userId, userIdOrig);
        //delete cache data in db
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);
        //update cache
        return loadCartCache(userId);
    }


    /**
     * helper method
     *
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId) {
        //read from sql
        List<CartInfo> cartInfos = cartInfoMapper.selectCartListBySkuPrice(userId);

        if (cartInfos != null && cartInfos.size() > 0) {
            //write cache
            Jedis jedis = redisUtil.getJedis();
            HashMap<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            String cartKey = "cart:" + userId + ":info";
            jedis.decr(cartKey);  //清除缓存，重新做
            jedis.hmset(cartKey, cartMap);
            jedis.expire(cartKey, 60 * 60 * 24);
            jedis.close();
        }
        return cartInfos;
    }

    /**
     * helper method: keep data persistence
     *
     * @param userId
     */
    public void loadCartCacheIfNotExits(String userId) {
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        jedis.expire(cartKey, ttl.intValue()+ 10);  //增加10s expire time
        Boolean exists = jedis.exists(cartKey);
        jedis.close();
        if (!exists) {
            loadCartCache(userId);
        }
    }


    @Override
    public void checkCart(String userId, String skuId, String isChecked) {

        //check if cache is exist, and add expire time
        loadCartCacheIfNotExits(userId);


        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);

        //save and update isChecked in Redis
        cartInfo.setIsChecked(isChecked);
        String jsonString = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey, skuId, jsonString);

        //create another jedis part to save all checked components
        String cartCheckedKey = "cart:" + userId + ":checked";
        if(isChecked.equals("1")){
            jedis.hset(cartCheckedKey,skuId,cartInfoJson);
            jedis.expire(cartCheckedKey,60*60);
        }else{
            jedis.hdel(cartCheckedKey,skuId);
        }


    }


}
