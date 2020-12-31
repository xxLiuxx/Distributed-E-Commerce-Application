package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsPageController {
    @Autowired
    private GoodsPageService goodsPageService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping("{spuId}.html")
    public String toItemPage(@PathVariable("spuId") Long spuId, Model model) {
        Map<String, Object> dataMap = this.goodsPageService.loadData(spuId);

        model.addAllAttributes(dataMap);

        goodsHtmlService.createHtml(spuId);
        return "item";
    }
}
