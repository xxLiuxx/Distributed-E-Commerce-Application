package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public interface GoodsApi {
    /**
     * spu商品分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public PageResult<SpuBo> querySpuBoByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );

    /**
     * 获取spuDetail
     * @param spuID
     * @return
     */
    @GetMapping("spu/detail/{spuID}")
    public SpuDetail querySpuDetailBySpuID(@PathVariable("spuID") Long spuID);

    /**
     * 根据spuID查询sku列表
     * @param spuID
     * @return
     */
    @GetMapping("sku/list")
    public List<Sku> querySkusBySpuID(@RequestParam("id")  Long spuID);

    /**
     * 根据id查询spu
     * @param spuId
     * @return
     */
    @GetMapping("{spuId}")
    public Spu querySpuById(@PathVariable("spuId") Long spuId);

    /**
     * query sku by skuId
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public Sku querySkuBySkuId(@PathVariable("skuId") Long skuId);
}
