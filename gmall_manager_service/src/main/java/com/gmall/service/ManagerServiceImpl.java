package com.gmall.service;

import com.alibaba.dubbo.config.annotation.Service;
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
import com.gmall.serviceimpl.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

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

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValues);
        //分别放进去两个list的内容，然后再return
        return skuInfo;
    }
}
