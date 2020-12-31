package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/brand")
public interface BrandApi {

    /**
     * 根据商品id查询商品
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Brand queryBrandByBrandId(@PathVariable Long id);
}
