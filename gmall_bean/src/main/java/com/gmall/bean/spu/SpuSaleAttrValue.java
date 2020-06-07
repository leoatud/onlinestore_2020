package com.gmall.bean.spu;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SpuSaleAttrValue implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String saleAttrId;
    @Column
    private String saleAttrValueName;

    @Transient
    private String isChecked;
}
