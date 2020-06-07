package com.gmall.bean.spu;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SpuSaleAttr implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String spuId;
    @Column
    private String saleAttrId;
    @Column
    private String saleAttrName;

    @Transient
    private List<SpuSaleAttrValue> spuSaleAttrValueList;
}
