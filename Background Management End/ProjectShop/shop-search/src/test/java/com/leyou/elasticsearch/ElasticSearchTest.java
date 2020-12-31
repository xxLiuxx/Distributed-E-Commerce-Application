package com.leyou.elasticsearch;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {
    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchOperations elasticsearchRestTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void createIndex() {
        this.elasticsearchRestTemplate.indexOps(Goods.class).create();
        Document mapping = this.elasticsearchRestTemplate.indexOps(Goods.class).createMapping();
        this.elasticsearchRestTemplate.indexOps(Goods.class).putMapping(mapping);
    }

    @Test
    public void insert() {
        Integer page = 1;
        Integer row = 100;

        do {
            // 分批查询spu
            PageResult<SpuBo> spuBoByPage = this.goodsClient.querySpuBoByPage(null, null, page, row);
            List<SpuBo> items = spuBoByPage.getItems();

            //spuBo -> goods
            List<Goods> goods = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoodsFromSpu(spuBo);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            this.goodsRepository.saveAll(goods);

            page++;
            row = items.size();
        } while (row == 100);
    }
}
