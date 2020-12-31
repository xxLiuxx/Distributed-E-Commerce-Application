package com.leyou.search.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SHOP.SEARCH.SAVE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}
    ))
    public void save(Long spuId) throws JsonProcessingException {
        if(spuId == null) {
            return;
        }
        this.searchService.save(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SHOP.SEARCH.DELETE.QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP.ITEM.EXCHANGE", ignoreDeclarationExceptions = "true"),
            key = {"item.delete"}
    ))
    public void delete(Long spuId) {
        if(spuId == null) {
            return;
        }
        this.searchService.delete(spuId);
    }
}
