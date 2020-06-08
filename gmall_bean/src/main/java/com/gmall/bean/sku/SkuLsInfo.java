package com.gmall.bean.sku;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * This is for jest use, ElasticSearch bean
 */
@Data
@NoArgsConstructor
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    Long hotScore = 0L;

    List<SkuLsAttrValue> skuAttrValueList;

}
