package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据pid查询分类
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        if(pid == null || pid < 0) {
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
        if(CollectionUtils.isEmpty(categories)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(categories);
    }

    /**
     * 编辑商品
     * @param brand_id
     * @return
     */
    @GetMapping("bid/{brand_id}")
    public ResponseEntity<List<Category>> editItem(@PathVariable(value = "brand_id") Long brand_id) {
        List<Category> list = this.categoryService.queryByBrandId(brand_id);
        if(list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);

    }

    /**
     * 根据cid列表查询所有分类名
     * @param ids
     * @return
     */
    @GetMapping
    public ResponseEntity<List<String>> queryCategoryByIdList(@RequestParam("ids") List<Long> ids) {
        List<String> names = this.categoryService.queryCategoryByIdList(ids);
        if(CollectionUtils.isEmpty(names)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
    }
}
