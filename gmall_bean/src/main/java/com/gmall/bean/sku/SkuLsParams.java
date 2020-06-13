package com.gmall.bean.sku;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SkuLsParams implements Serializable {
    String keyword;
    String catalog3Id;
    String[] valueId;
    int pageNo = 1;
    int pageSize = 20;
}
