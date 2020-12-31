package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final String USER_PREFIX = "user:verify:";
    /**
     * type is 1, check by username; type is 2 check by phone number
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if(type == 1) {
            user.setUsername(data);
        } else if(type == 2) {
            user.setPhone(data);
        } else {
            return null;
        }

        return this.userMapper.selectCount(user) == 0;
    }

    /**
     * 发送验证码
     * @param phone
     */
    public void sendVerifyCode(String phone) {
        if(StringUtils.isBlank(phone)) {
            return;
        }
        //生成验证码
        String code = NumberUtils.generateCode(6);
        System.out.println(code);

        //发送信息到rabbitmq
        Map<String, String> message = new HashMap<>(2);
        message.put("phone", phone);
        message.put("code", code);

        amqpTemplate.convertAndSend("SHOP.SMS.EXCHANGE", "sms.verify.code", message);

        //将验证码存入redis
        redisTemplate.opsForValue().set(USER_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        System.out.println("code sent");

    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    public void reigster(User user, String code) {
        // 验证码是否相同
        if(code == null || !StringUtils.equals(code, redisTemplate.opsForValue().get(USER_PREFIX + user.getPhone()))) {
            return;
        }
        // 获取盐，对密码进行加密
        String salt = CodecUtils.generateSalt();
        String encryptedPassword = CodecUtils.md5Hex(user.getPassword(), salt);

        // 保存入数据库
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setCreated(new Date());
        user.setId(null);

        boolean bool = this.userMapper.insertSelective(user) == 1;

        if(bool) {
            this.redisTemplate.delete(USER_PREFIX + user.getPhone());
        } else {
            return;
        }
    }

    public User query(String username, String password) {
        User user = new User();
        user.setUsername(username);
        User selectOne = this.userMapper.selectOne(user);

        if(selectOne == null) {
            return null;
        }

        String salt = selectOne.getSalt();
        password = CodecUtils.md5Hex(password, salt);
        if(StringUtils.equals(password, selectOne.getPassword())) {
            return selectOne;
        }
        return null;
    }
}
