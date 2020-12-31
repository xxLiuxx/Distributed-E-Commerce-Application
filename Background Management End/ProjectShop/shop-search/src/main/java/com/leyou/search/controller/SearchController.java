package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest searchRequest) {
        SearchResult goodsPage = this.searchService.search(searchRequest);

        if(goodsPage == null || CollectionUtils.isEmpty(goodsPage.getItems())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goodsPage);
    }
}
