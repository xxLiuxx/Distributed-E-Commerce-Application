package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    //使用ThreadLocal存放当前用户信息，保证线程安全
    private static ThreadLocal<UserInfo> THREAD_LOCAL =  new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    //前置拦截，用于获取用户信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        try {
            //获取用户信息
            UserInfo user = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

            THREAD_LOCAL.set(user);

            return true;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    //后置拦截器用于清理数据
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //ThreadLocal在线程结束时会自动垃圾回收，但是这里使用的是tomcat线程池中的线程
        //使用完会返回到线程池，所以要手动进行清理
        THREAD_LOCAL.remove();
    }
}
