package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;

import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER= new ObjectMapper();

    public Goods buildGoodsFromSpu(Spu spu) throws JsonProcessingException {
        Goods goods = new Goods();

        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());

        // 标题、分类、品牌名的拼接
        List<String> cnames = this.categoryClient.queryCategoryByIdList(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        String categoryNames = StringUtils.join(cnames, " ");
        Brand brand = this.brandClient.queryBrandByBrandId(spu.getBrandId());
        goods.setAll(spu.getTitle() + " " + categoryNames + " " + brand.getName());

        // 需要获取价格的集合
        List<Long> prices = new ArrayList<Long>();
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        List<Sku> skus = this.goodsClient.querySkusBySpuID(spu.getId());
        for (Sku sku : skus) {
            prices.add(sku.getPrice());
            //  sku中字段太多，我们只需要部分，所以使用map来存储需要的
            HashMap<String, Object> map = new HashMap<>(4);
            map.put("id", sku.getId());
            map.put("price", sku.getPrice());
            map.put("title", sku.getTitle());
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        }
        goods.setPrice(prices);

        // 需要获取sku列表，转为json字符串
        goods.setSkus(MAPPER.writeValueAsString(skus));

        // 获取{key:value}形式的参数集合
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        // 将spuDetail中的generic_spec, special_spec反序列化为map
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuID(spu.getId());
        String genericSpec = spuDetail.getGenericSpec();
        String specialSpec = spuDetail.getSpecialSpec();
        Map<String, Object> genericMap = MAPPER.readValue(genericSpec, new TypeReference<Map<String, Object>>(){});
        Map<String, List<Object>> specMap = MAPPER.readValue(specialSpec, new TypeReference<Map<String, List<Object>>>(){});
        // 遍历所有的规格参数，放入一个map
        Map<String, Object> map = new HashMap<>();
        for(SpecParam param : params) {
            if(param.getGeneric()) {
                String value = genericMap.get(param.getId().toString()).toString();
                // 如果参数是数字类型，需要变为区间搜索
                if(param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                map.put(param.getName(), value);
            } else {
                map.put(param.getName(), specMap.get(param.getId().toString()));
            }
        }
        goods.setSpecs(map);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)) {
            return null;
        }
        // 构造查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        BoolQueryBuilder basicQuery = buildBoolQuery(searchRequest);
        queryBuilder.withQuery(basicQuery);
        // 添加分页， 默认从0开始
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));
        // 指定需要的字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));

        // 添加分类和品牌的聚合
        String categoryAggName = "categoryAgg";
        String brandAggName = "brandAgg";

        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 查询
        AggregatedPage<Goods> goods = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        // 解析聚合结果集，获取需要的集合
        List<Brand> brands = getBrandAggResult(goods.getAggregation(brandAggName));
        List<Map<String, Object>> categories = getCategoryAggResult(goods.getAggregation(categoryAggName));

        // 添加规格参数的聚合，只有分类数量为1才进行聚合
        List<Map<String, Object>> specs = new ArrayList<>();
        if(categories.size() == 1) {
            specs = getSpecsAggResult((Long) categories.get(0).get("id"), basicQuery);
        }

        return new SearchResult(goods.getTotalElements(), goods.getTotalPages(), goods.getContent(), brands, categories, specs);
    }

    private BoolQueryBuilder buildBoolQuery(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));

        if(CollectionUtils.isEmpty(searchRequest.getFilter())) {
            return queryBuilder;
        }

        //获取filter
        Map<String, Object> filter = searchRequest.getFilter();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            if(StringUtils.equals(key, "品牌")) {
                key = "brandId";
            } else if(StringUtils.equals(key, "分类")) {
                key = "cid3";
            } else {
                key = "specs." + key + ".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return queryBuilder;
    }

    /**
     * 规格参数聚合搜索
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getSpecsAggResult(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specAggResult = new ArrayList<>();

        //先获取参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);

        // 添加查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        params.forEach(specParam -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
        });

        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        AggregatedPage<Goods> specPage = (AggregatedPage) this.goodsRepository.search(queryBuilder.build());

        // key-聚合名称(规格参数名) value-聚合对象
        Map<String, Aggregation> aggregationMap = specPage.getAggregations().asMap();

        // 遍历map，解析聚合
        for(Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String, Object> temp = new HashMap<>();
            // 添加规格参数聚合名称
            temp.put("type", entry.getKey());
            // 获取bucket中的值，加入集合，添加为对应的过滤条件
            List<String> options = new ArrayList<>();
            Aggregation value = entry.getValue();
            List<? extends Terms.Bucket> buckets = ((Terms) value).getBuckets();
            buckets.forEach(bucket -> {
                String spec = bucket.getKeyAsString();
                options.add(spec);
            });

            temp.put("options", options);

            specAggResult.add(temp);
        }

        return specAggResult;
    }

    /**
     * 解析分类聚合结果
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        Terms terms = (Terms) aggregation;
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = new ArrayList<>();

        List<? extends Terms.Bucket> buckets = terms.getBuckets();

        buckets.forEach(bucket -> {
            Long cid3 = bucket.getKeyAsNumber().longValue();
            cids.add(cid3);
        });
        List<String> cnames = this.categoryClient.queryCategoryByIdList(cids);

        for(int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", cnames.get(i));
            categories.add(map);
        }
        return categories;
    }

    /**
     * 解析品牌聚合结果
     * @param aggregation
     * @return
     */
    public List<Brand> getBrandAggResult(Aggregation aggregation) {
        Terms terms = (Terms) aggregation;
        List<Brand> brands = new ArrayList<>();

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            Long brandId = bucket.getKeyAsNumber().longValue();
            Brand brand = this.brandClient.queryBrandByBrandId(brandId);
            brands.add(brand);
        });

        return brands;
    }

    public void save(Long spuId) throws JsonProcessingException {
        Spu spu = this.goodsClient.querySpuById(spuId);
        Goods goods = buildGoodsFromSpu(spu);
        this.goodsRepository.save(goods);
    }

    public void delete(Long spuId) {
        this.goodsRepository.deleteById(spuId);
    }
}
