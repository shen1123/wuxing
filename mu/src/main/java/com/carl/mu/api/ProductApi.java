package com.carl.mu.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.carl.mu.sup.ApiSup;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author WIN10
 * @date 2021-07-14 16:18
 */
@Controller
public class ProductApi {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @RequestMapping("test")
    public void test() {
        ApiSup apiSup = new ApiSup();
        apiSup.setId(1);
        apiSup.setMsg(null);
        rabbitTemplate.convertAndSend("rabbit", JSON.toJSONString(apiSup, SerializerFeature.WriteNullStringAsEmpty));
    }

    @ResponseBody
    @RequestMapping("create")
    public void create() {

    }
}
