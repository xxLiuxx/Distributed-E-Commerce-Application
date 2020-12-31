package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsPageService goodsPageService;

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(this.goodsPageService.loadData(spuId));

        File file = new File("/usr/local/Cellar/nginx/1.19.2/html/item/" + spuId + ".html");
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(file);
            templateEngine.process("item", context, printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    public void delete(Long spuId) {
        File file = new File("/usr/local/Cellar/nginx/1.19.2/html/item/" + spuId + ".html");
        file.deleteOnExit();
    }
}
