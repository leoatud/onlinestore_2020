package com.gmall.bean.cart;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartInfo implements Serializable {
    @Id  //primary key
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;
    @Column
    String userId;
    @Column
    String skuId;
    @Column
    BigDecimal cartPrice;
    @Column
    Integer skuNum;
    @Column
    String imgUrl;
    @Column
    String skuName;


    @Transient
    BigDecimal skuPrice;  //最新的价格，如果价格变了，需要关联skuInfo
    @Transient
    String isChecked="0";   //前面的勾


}
