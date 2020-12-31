package com.leyou.auth;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JwtTest {

    private static final String pubKeyPath = "/Users/liuyuchen/Desktop/乐优商城-11月版/leyou/day17-授权中心/rsa/rsa.pub";

    private static final String priKeyPath = "/Users/liuyuchen/Desktop/乐优商城-11月版/leyou/day17-授权中心/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTYwODg2Njc0NH0.T0xW7Nwz_nzAJQQaPxpXLXqkwtY8FRZtd7DXazknAslOFNKw_K0w8UZGIf5NQ-Df-NNe3R_ouR3gD0YaIT5LNXzFtROvyoP3n-j0YFLn75g1A3guc8kO6mGgRgJ_QJ-DB53Uoiy4d2q9AgD9F3pli8lKZXQ9fW_NZrixOvhpK_4";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
