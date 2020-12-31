package com.leyou.item.service;

import com.leyou.item.pojo.Category;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoriesByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return this.categoryMapper.select(category);
    }

    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    /**
     * 根据ID列表查询分类
     * @param ids
     * @return
     */
    public List<String> queryCategoryByIdList(List<Long> ids) {
        //根据id列表查询分类
        List<Category> categories = this.categoryMapper.selectByIdList(ids);

        //将分类列表转为分类名列表
        List<String> cnames = categories.stream().map(category -> category.getName()).collect(Collectors.toList());

        return cnames;
    }
}
