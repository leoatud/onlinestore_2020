package com.gmall.bean.sku;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SkuSaleAttrValue implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String skuId;
    @Column
    private String saleAttrId;
    @Column
    private String saleAttrValueId;
    @Column
    private String saleAttrName;
    @Column
    private String saleAttrValueName;

}
