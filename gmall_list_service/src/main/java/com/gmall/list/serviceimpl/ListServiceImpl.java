package com.gmall.list.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.sku.SkuLsInfo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Service
public class ListServiceImpl {

    @Autowired
    JestClient jestClient;

    public void saveSkuListInfo(SkuLsInfo skuLsInfo){
        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        indexBuilder.index("gmall_sku_info").type("_doc").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
