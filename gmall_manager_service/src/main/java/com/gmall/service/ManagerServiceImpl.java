package com.gmall.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.base.*;
import com.gmall.bean.sku.SkuAttrValue;
import com.gmall.bean.sku.SkuImage;
import com.gmall.bean.sku.SkuInfo;
import com.gmall.bean.sku.SkuSaleAttrValue;
import com.gmall.bean.spu.SpuImage;
import com.gmall.bean.spu.SpuInfo;
import com.gmall.bean.spu.SpuSaleAttr;
import com.gmall.bean.spu.SpuSaleAttrValue;
import com.gmall.mapper.base.*;
import com.gmall.mapper.sku.SkuAttrValueMapper;
import com.gmall.mapper.sku.SkuImageMapper;
import com.gmall.mapper.sku.SkuInfoMapper;
import com.gmall.mapper.sku.SkuSaleAttrValueMapper;
import com.gmall.mapper.spu.SpuImageMapper;
import com.gmall.mapper.spu.SpuInfoMapper;
import com.gmall.mapper.spu.SpuSaleAttrMapper;
import com.gmall.mapper.spu.SpuSaleAttrValueMapper;
import com.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Component
public class ManagerServiceImpl implements ManagerService {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
//        Example example = new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id", catalog3Id);
//
//        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectByExample(example);
//        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
//            baseAttrInfo.setAttrValueList(baseAttrValueList);
//        }
//        return baseAttrInfos;
        /**
         * use one big sql to do the database access is better
         */
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //handle duplicate primary key situation
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            //if primary key not exist, then insert, else--> update only
            //handle the info table
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //delete and then add from zero
        //全部删除，再统一保存
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);

        //handle the value table, including more details key-value
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        String id = baseAttrInfo.getId(); //one to many
        for (BaseAttrValue attrValue : attrValueList) {
            attrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(attrValue);
        }
    }


    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(select);
        return baseAttrInfo;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        return null;
    }

    /**
     * SPU logic start here =======================================
     */
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu basic info
        spuInfoMapper.insertSelective(spuInfo);
        //image info
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);  //put new image items
        }
        //sale properties
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            //sale properties value
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue attrValue : spuSaleAttrValueList) {
                attrValue.setSaleAttrId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdCheckSku(String spuId, String skuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuIdCheckSku(spuId, skuId);
    }


    /**
     * SKU logic start here =======================================
     */
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //save, four part, big save
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //second properties
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }

        //sale attr
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }

        //image url
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage image : skuImageList) {
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(image);
        }
    }

    public SkuInfo getSkuInfoDb(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo == null) return null;
        //image
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValues);

        //平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        Map skuValueIds = new HashMap();
        for (Map map : mapList) {
            String skuId = map.get("sku_id") + "";  //no matter get long or string, will be convert to string
            String valueIds = (String) map.get("value_ids");
            skuValueIds.put(valueIds, skuId);
        }
        return skuValueIds;
    }

    /**
     * Redis usage demo ==================================
     */
    @Autowired
    private RedisUtil redisUtil;

    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_INFO_SUFFIX = ":info";
    public static final String SKUKEY_LOCK_SUFFIX = "：lock";
    public static final int SKU_EXPIRE = 10;  // Cache, valid time

    //access through redis
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        SkuInfo skuInfo = new SkuInfo();
        if (skuInfoJson != null) {
            System.out.println("Redis found cache");
            skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
        } else {
            //setnx here
            //type key value -->写进去一个任何的内容，当成lock，用来判断
            //自定义一个lock，写进redis里面
            String lockKey = SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUFFIX;
            //Long locked = jedis.setnx(lockKey, "locked");
            String token = UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 10);

            if ("OK".equals(locked)) {
                //if not found--> go check db
                skuInfo = getSkuInfoDb(skuId);
                //write new result into redis
                String s = JSON.toJSONString(skuInfo);
                jedis.setex(skuKey, SKU_EXPIRE, s);
                if (jedis.exists(lockKey) && jedis.get(lockKey).equals(token)) {
                    jedis.del(lockKey);
                }
            } else {
                //sleep thread, and then call itself, recursion
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuInfo(skuId);
            }
        }
        jedis.close();
        return skuInfo;
    }

    /**
     * Redission: higher level of redis
     */
    public SkuInfo getSkuInfo_redisson(String skuId) {
        //底层实现还是redis, 因此不用变，只需要更改涉及到🔐的地方
        Jedis jedis = redisUtil.getJedis();
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        String lockKey = SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUFFIX;

        SkuInfo skuInfo = new SkuInfo();
        if (skuInfoJson != null) {
            System.out.println("Redis found cache");
            skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
        } else {
            Config config = new Config(); // 选择redisson的
            //因为只在local运行，所以用127
            config.useSingleServer().setAddress("redis://127.0.0.1:6379");
            RedissonClient redissonClient = Redisson.create(config);

            RLock lock = redissonClient.getLock(lockKey);
            //lock.lock(10, TimeUnit.SECONDS);  //强制上锁
            try {
                lock.tryLock(10,5,TimeUnit.SECONDS);  //尝试上锁，可以timeout
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            skuInfo = getSkuInfoDb(skuId);
            String s = JSON.toJSONString(skuInfo);
            jedis.setex(skuKey, SKU_EXPIRE, s);

            lock.unlock(); //释放锁

        }
        return null;
    }


    @Override
    public List<BaseAttrInfo> getAttrList(List attrValueIdList) {
        //list --> xx,xx,xx
        String join = StringUtils.join(attrValueIdList.toArray(), ",");
        return baseAttrInfoMapper.getBaseAttrInfoListByValueIds(join);
    }
}
