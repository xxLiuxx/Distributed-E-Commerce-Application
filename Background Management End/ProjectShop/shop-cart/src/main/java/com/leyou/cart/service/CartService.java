package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String USER_PREFIX = "user:cart:";

    public void addCart(Cart cart) {
        //获取user信息
        UserInfo user = LoginInterceptor.getUserInfo();
        String key = USER_PREFIX + user.getId();

        //userID为key，查询redis中的carts集合
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();

        //如果redis中有该cart，更新数量
        if(hashOperations.hasKey(skuId.toString())) {
            String cartJson = hashOperations.get(skuId.toString()).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(num + cart.getNum());
            hashOperations.put(skuId.toString(), JsonUtils.serialize(cart));
        } else {
            //如果没有，加入该cart
            //根据skuId查询
            Sku sku = this.goodsClient.querySkuBySkuId(skuId);
            cart.setImage(StringUtils.isBlank(sku.getImages())? "" : sku.getImages().split(",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setUserId(user.getId());
            hashOperations.put(skuId.toString(), JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCarts() {
        UserInfo user = LoginInterceptor.getUserInfo();

        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(USER_PREFIX + user.getId());

        if(!redisTemplate.hasKey(USER_PREFIX + user.getId())) {
            return null;
        }

        List<Object> cartJson = hashOperations.values();

        if(CollectionUtils.isEmpty(cartJson)) {
            return null;
        }

        List<Cart> carts = cartJson.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    public void updateCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(USER_PREFIX + userInfo.getId());

        String cartJson = hashOperations.get(skuId.toString()).toString();
        cart = JsonUtils.parse(cartJson, Cart.class);
        cart.setNum(num);
        hashOperations.put(skuId.toString(), JsonUtils.serialize(cart));
    }
}
