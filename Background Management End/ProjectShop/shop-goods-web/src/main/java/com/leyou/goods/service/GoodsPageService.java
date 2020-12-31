package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsPageService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 将所需要的数据集合以Map的结构组合
     * @param spuId
     * @return
     */
    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> model = new HashMap<>();

        // 获取当前页面的spu
        Spu spu = this.goodsClient.querySpuById(spuId);

        //获取spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuID(spu.getId());

        //获取当前spu下的sku集合
        List<Sku> skus = this.goodsClient.querySkusBySpuID(spu.getId());

        //获取brand
        Long brandId = spu.getBrandId();
        Brand brand = this.brandClient.queryBrandByBrandId(brandId);

        //查询分类
        List<Long> idList = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> cnames = this.categoryClient.queryCategoryByIdList(idList);
        List<Map<String, Object>> categories = new ArrayList<>();
        for(int i = 0; i < idList.size(); i++) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("id", idList.get(i));
            temp.put("name", cnames.get(i));
            categories.add(temp);
        }


        //获取参数组，查询特殊规格参数
        List<SpecGroup> groups = this.specificationClient.querySpecGroupsWithParams(spu.getCid3());
        List<SpecParam> specParams = this.specificationClient.queryParams(null, spu.getCid3(), false, null);

        //将参数组id与名字对应，存为paramMap(id, name)
        Map<Long, String> paramMap = new HashMap<>();
        specParams.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });

        List<SpecParam> genericParams = this.specificationClient.queryParams(null, spu.getCid3(), true, null);
        Map<Long, String> genericMap = new HashMap<>();
        genericParams.forEach(param -> {
            genericMap.put(param.getId(), param.getName());
        });

        model.put("spu", spu);
        model.put("spuDetail", spuDetail);
        model.put("categories", categories);
        model.put("skus", skus);
        model.put("brand", brand);
        model.put("groups", groups);
        model.put("paramMap", paramMap);
        model.put("genericMap", genericMap);
        return model;
    }

}
