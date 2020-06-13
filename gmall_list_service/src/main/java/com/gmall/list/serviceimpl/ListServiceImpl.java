package com.gmall.list.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.sku.SkuLsInfo;
import com.gmall.bean.sku.SkuLsParams;
import com.gmall.bean.sku.SkuLsResult;
import com.gmall.service.ListService;
import com.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuListInfo(SkuLsInfo skuLsInfo) {
        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        indexBuilder.index("gmall_sku_info").type("_doc").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        //********************
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (skuLsParams.getKeyword() != null) {
            boolQueryBuilder.must(new MatchQueryBuilder("skuName", skuLsParams.getKeyword()));
        }
        //boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        searchSourceBuilder.query(boolQueryBuilder);
        String[] valueIds = skuLsParams.getValueId();
        for (int i = 0; i < valueIds.length; i++) {
            String valueId = valueIds[i];
            boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valudId", valueId));
        }
        //********************


        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());
        Search build = searchBuilder.addIndex("gmall_sku_info").addType("_doc").build();

        //return result
        SkuLsResult skuLsResult = new SkuLsResult();

        try {
            List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
            SearchResult searchResult = jestClient.execute(build);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;
                String skuNameHL = hit.highlight.get("skuName").get(0);
                skuLsInfo.setSkuName(skuNameHL); // 带有<span>hightlight</span>的string
                skuLsInfoList.add(skuLsInfo);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);
            Long total = searchResult.getTotal();
            skuLsResult.setTotal(total);
            long totalPage = (total + skuLsParams.getPageSize() - 1) / skuLsParams.getPageNo();
            skuLsResult.setTotalPages(totalPage);

            List<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsResult;
    }

    /**
     * 处理redis达到hit=100了，后端这边+1，可以根据热度将数据排序
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(String skuId) {
        //设计key, type, value (from五大类型)
        Jedis jedis = redisUtil.getJedis();

        String hotScoreKey = "sku:" + skuId + ":hotscore";
        Long hotScore = jedis.incr(hotScoreKey);
        if (hotScore % 10 == 0) {
            updateHotScoreEs(skuId, hotScore);
        }
    }

    public void updateHotScoreEs(String skuId, Long hotScore){
        //TODO
        String updateQuery = "";

        Update build = new Update.Builder(updateQuery).index("gmall_sku_info").type("_doc").id(skuId).build();

        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
