package com.gmall.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.order.OrderInfo;
import com.gmall.bean.order.ProcessStatus;
import com.gmall.service.OrderService;
import com.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderConsumer {
    @Reference
    OrderService orderService;
    @Reference
    ActiveMQUtil activeMQUtil;


    @JmsListener(destination = "PAYMENT_TO_ORDER", containerFactory = "jmsQueueListener")
    public void consumerPayment(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if ("success".equals(result)) {
            //先修改
            orderService.updateStatus(orderId, ProcessStatus.PAID);
            //发送MQ--> stock/warehourse module
            sendOrderToWare(orderId);
        }
    }


    public void sendOrderToWare(String orderId) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        Map paramMap = new HashMap();
        //将所有orderInfo的内容都放到map里面
        //omitted
        String jsonString = JSON.toJSONString(paramMap);

        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer producer = session.createProducer(session.createQueue("ORDER_RESULT_QUEUE"));
            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(jsonString);

            orderService.updateStatus(orderId, ProcessStatus.NOTIFIED_WARE);

            producer.send(message);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * warehouse发货后更新，需要接受更新消息: 以下两个 method都是
     *
     * @param message
     */
    @JmsListener(destination = "SKU_DEDECT_QUEUE", containerFactory = "jsmQueueListener",concurrency = "5")
    public void consumeWareDeduct(MapMessage message) throws JMSException {
        //Connection connection = activeMQUtil.getConnection();
        String orderId = message.getString("orderId");
        String status = message.getString("status");
        if ("DEDECT".equals(status)) {
            orderService.updateStatus(orderId, ProcessStatus.WAITING_DELEVER);
        } else {
            orderService.updateStatus(orderId, ProcessStatus.STOCK_EXCEPTION);
        }
    }

    @JmsListener(destination = "SKU_DELIVER_QUEUE", containerFactory = "jsmQueueListener",concurrency = "5")
    public void consumeDeliver(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String trackingNo = mapMessage.getString("trackingNo");
        String status = mapMessage.getString("status");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTrackingNo(trackingNo);

        if ("DELIEVERED".equals(status)) {
            orderService.updateStatus(orderId, ProcessStatus.DELEVERED, orderInfo);
        }
    }

}
