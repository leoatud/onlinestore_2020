package com.gmall.service;

import com.gmall.bean.sku.SkuLsInfo;
import com.gmall.bean.sku.SkuLsParams;
import com.gmall.bean.sku.SkuLsResult;

/**
 * For Elasticsearch use only
 */
public interface ListService {
    void saveSkuListInfo(SkuLsInfo skuLsInfo);

    SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams);

    void incrHotScore(String skuId);
}
