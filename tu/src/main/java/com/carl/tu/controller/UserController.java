package com.carl.tu.controller;

import com.alibaba.fastjson.JSON;
import com.carl.tu.conf.ESHighLevelRESTClientTool;
import com.carl.tu.entity.User;
import com.carl.tu.service.UserService;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author WIN10
 * @date 2021-07-12 15:20
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("get")
    @ResponseBody
    public User fetchUser(Integer id) {
        return userService.selectById(id);
    }

    @RequestMapping("create")
    @ResponseBody
    public String create() {
        IndexRequest req = new IndexRequest();
        User user = new User();
        user.setId(1);
        IndexRequest req1 = ESHighLevelRESTClientTool.genInsertRequest("user", UUID.randomUUID().toString(), JSON.toJSONString(user));
        Map<String, Object> map = new HashMap<>();
        map.put("account","carl");
        map.put("password","123");
        map.put("createDate",new Date());
        IndexRequest req2 = ESHighLevelRESTClientTool.genInsertRequest("user", UUID.randomUUID().toString(), map);
        BulkRequest request = new BulkRequest().add(req2).add(req1);
        BulkResponse response = ESHighLevelRESTClientTool.bulkSyn(request);
        if (response.hasFailures()) {
            return response.buildFailureMessage();
        }
//        XContentBuilder builder = new XContentBuilder(FileUtils.openOutputStream("D:\\Test\\wu-xing\\tu\\src\\main\\resources\\bootstrap.yml"));
//        ESHighLevelRESTClientTool.genInsertRequest("user", UUID.randomUUID().toString(), );
        return response.status().name();
    }

}
