package com.leyou.order.service.api;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "shop-gateway", path = "/api/item")
public interface GoodsService extends GoodsApi {
}
