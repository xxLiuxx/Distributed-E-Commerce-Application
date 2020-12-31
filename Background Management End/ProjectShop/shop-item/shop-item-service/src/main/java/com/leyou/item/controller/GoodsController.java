package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * spu商品分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    ) {
        PageResult<SpuBo> pageResult = this.goodsService.querySpuByPage(key, saleable, page, rows);

        if(CollectionUtils.isEmpty(pageResult.getItems())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 保存商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo) {
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新商品
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo) {
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 获取spuDetail
     * @param spuID
     * @return
     */
    @GetMapping("spu/detail/{spuID}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuID(@PathVariable("spuID") Long spuID) {
        SpuDetail spuDetail  = this.goodsService.querySpuDetailBySpuID(spuID);

        if(spuDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spuID查询sku列表
     * @param spuID
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuID(@RequestParam("id")  Long spuID) {
        List<Sku> skus = this.goodsService.querySkusBySpuID(spuID);

        if(CollectionUtils.isEmpty(skus)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(skus);
    }

    /**
     * 根据id查询spu
     * @param spuId
     * @return
     */
    @GetMapping("{spuId}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("spuId") Long spuId) {
        Spu spu = this.goodsService.querySpuById(spuId);
        if(spu == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spu);
    }

    /**
     * query sku by skuId
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId") Long skuId) {
        Sku sku = this.goodsService.querySkuBySkuId(skuId);
        return ResponseEntity.ok(sku);
    }
}
