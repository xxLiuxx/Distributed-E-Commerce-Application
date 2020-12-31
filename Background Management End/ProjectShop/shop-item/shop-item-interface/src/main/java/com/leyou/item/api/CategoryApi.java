package com.leyou.item.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {
    /**
     * 根据cid列表查询所有分类名
     * @param ids
     * @return
     */
    @GetMapping
    public List<String> queryCategoryByIdList(@RequestParam("ids") List<Long> ids);
}
