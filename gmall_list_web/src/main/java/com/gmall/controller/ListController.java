package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.base.BaseAttrInfo;
import com.gmall.bean.base.BaseAttrValue;
import com.gmall.bean.sku.SkuLsParams;
import com.gmall.bean.sku.SkuLsResult;
import com.gmall.service.ListService;
import com.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;
    @Reference
    ManagerService managerService;


    @GetMapping("/list.html")
    @ResponseBody
    public String getList(SkuLsParams skuLsParams, Model model) {
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsResult", skuLsResult);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = managerService.getAttrList(attrValueIdList);
        model.addAttribute("attrList", attrList);
        //return JSON.toJSONString(skuLsResult);

        //for breadcrumbs
        List<BaseAttrValue> selectedValueList = new ArrayList<>();


        //因为attr涉及到实时操作，如果选择清单的时候属性来回变化
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String s = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals((s))) {
                            iterator.remove();

                            String paramUrl = makeParamUrl(skuLsParams, s); //面包屑取消路径
                            baseAttrValue.setParamUrl(paramUrl);
                            selectedValueList.add(baseAttrValue);
                        }
                    }
                }
            }
        }
        //history url
        model.addAttribute("paramURL", makeParamUrl(skuLsParams));

        //从查出来的东西里面筛选
        model.addAttribute("selectedValueList","");

        model.addAttribute("keyword",skuLsParams.getKeyword());

        //paging
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("totalPages",skuLsResult.getTotalPages());

        return "list";
    }

    private String makeParamUrl(SkuLsParams skuLsParams, String... excludeValueId) {
        String paramUrl = "";
        if (skuLsParams.getKeyword() != null) {
            paramUrl += "keyword=" + skuLsParams.getKeyword();
        } else if (skuLsParams.getCatalog3Id() != null) {
            paramUrl += "catalog3Id" + skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];

                if(excludeValueId!=null &&excludeValueId.length>0){
                    //需要排除的valueID
                    String s = excludeValueId[0];
                    //要排除和要添加的相等，则没有任何处理
                    if (valueId.equals(s)) continue;
                }

                if (paramUrl.length() > 0) {
                    paramUrl += "&";
                }
                paramUrl += "valueId=" + valueId;
            }
        }
        return paramUrl;
    }
}
