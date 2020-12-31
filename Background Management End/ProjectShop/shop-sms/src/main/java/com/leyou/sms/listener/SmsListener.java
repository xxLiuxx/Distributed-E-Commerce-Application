package com.leyou.sms.listener;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    private SmsProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SHOP.SMS.QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP.SMS.EXCHANGE", ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}
    ))
    public void sendMsg(Map<String, String> msg) throws ClientException {
        if(CollectionUtils.isEmpty(msg) || msg.size() <= 0) {
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if(StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            return;
        }

        this.smsUtil.sendSms(phone, code, prop.getSignName(), prop.getVerifyCodeTemplate());
    }
}
