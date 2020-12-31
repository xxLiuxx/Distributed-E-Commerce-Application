package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsPageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SHOP.ITEM.SAVE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void save(Long spuId) {
        if(spuId == null) {
            return;
        }
        this.goodsHtmlService.createHtml(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SHOP.ITEM.DELETE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void delete(Long spuId) {
        this.goodsHtmlService.delete(spuId);
    }
}
