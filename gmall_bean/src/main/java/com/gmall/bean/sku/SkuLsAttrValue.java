package com.gmall.bean.sku;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * this is for ElasticSearch used bean
 */
@Data
@NoArgsConstructor
public class SkuLsAttrValue implements Serializable {

    String valueId;
}
