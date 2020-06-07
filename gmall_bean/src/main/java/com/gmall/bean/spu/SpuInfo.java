package com.gmall.bean.spu;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SpuInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String spuName;
    @Column
    private String description;
    @Column
    private String catalog3Id;

    @Transient
    private List<SpuImage> spuImageList;

    @Transient
    private List<SpuSaleAttr> spuSaleAttrList;
}
