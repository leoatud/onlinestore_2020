package com.gmall.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.order.OrderDetail;
import com.gmall.bean.order.OrderInfo;
import com.gmall.bean.order.ProcessStatus;
import com.gmall.mapper.OrderDetailMapper;
import com.gmall.mapper.OrderInfoMapper;
import com.gmall.service.OrderService;
import com.gmall.util.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        //include orderdetails, and orderinfo，一个大的内容
        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    @Override
    public String genToken(String userId) {

        String token = UUID.randomUUID().toString();
        String tokenKey = "user:" + userId + ":trade_code";

        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tokenKey, 10 * 60, token);

        jedis.close();
        return token;
    }

    @Override
    public boolean verifyToken(String userId, String token) {
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        String tokenEx = jedis.get(tokenKey);
        jedis.watch(tokenKey);
        Transaction transaction = jedis.multi();
        if (tokenEx != null && tokenEx.equals(token)) {
            transaction.del(tokenKey);
        }
        List<Object> exec = transaction.exec();
        if (exec != null && exec.size() > 0) {
            jedis.close();
            return true;
        } else {
            jedis.close();
            return false;
        }
    }

    @Override
    public void delToken(String userId) {
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.del(tokenKey);
        jedis.close();
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    @Override
    public void updateStatus(String orderId, ProcessStatus processStatus, OrderInfo... orderInfos) {
        OrderInfo orderInfo = new OrderInfo();

        if(orderInfos!=null && orderInfos.length>0){
            orderInfo = orderInfos[0];
        }
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfo.setId(orderId);

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public List<Map> orderSplit(String orderId, String wareSkuMapJson) {

        OrderInfo orderInfoParent = getOrderInfo(orderId);

        List<Map> mapList = JSON.parseArray(wareSkuMapJson, Map.class);
        for (Map map : mapList) {
            OrderInfo orderInfoSub = new OrderInfo();
            try {
                BeanUtils.copyProperties(orderInfoSub,orderInfoParent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        return null;
    }
}
