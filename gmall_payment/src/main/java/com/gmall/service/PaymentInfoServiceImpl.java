package com.gmall.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.gmall.bean.order.PaymentInfo;
import com.gmall.bean.order.PaymentStatus;
import com.gmall.mapper.PaymentInfoMapper;
import com.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.xml.ws.soap.Addressing;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insert(paymentInfo);
    }

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    public void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo1) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo1, example);
    }

    public void sendPaymentToOrder(String orderId, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer producer = session.createProducer(session.createQueue("PAYMENT_TO_ORDER"));
            MapMessage message = new ActiveMQMapMessage();
            message.setString("orderId", orderId);
            message.setString("result", result);
            producer.send(message);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo) {
        //copied from ali sdk
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            //success
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())) {
                return PaymentStatus.PAID;
            }
        }
        return null;
    }

    /**
     * 三次检查信息，是否可以给response
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    public void sendDelayPaymentResult(String outTradeNo, Long delaySec, Integer checkCount){
        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage message = new ActiveMQMapMessage();
            //omitt message implementation

            producer.send(message);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jsmQueueListener")
    public void consumeDelayCheck(){

    }


}
