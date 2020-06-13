package com.gmall.bean.sku;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * For Elasticsearch return result use
 */
@Data
@NoArgsConstructor
public class SkuLsResult implements Serializable {


    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;
}
