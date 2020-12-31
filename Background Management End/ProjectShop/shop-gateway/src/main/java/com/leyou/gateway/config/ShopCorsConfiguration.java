package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class ShopCorsConfiguration {

    @Bean
    public CorsFilter getFilter() {

        //初始化配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        //允许跨域源
        configuration.addAllowedOrigin("http://manage.leyou.com");
        configuration.addAllowedOrigin("http://www.leyou.com");
        configuration.setAllowCredentials(true);
        //允许跨域的方法
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        //初始化配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(configurationSource);
    }

}
