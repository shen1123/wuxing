package com.carl.tu.api;

import com.carl.tu.conf.ESHighLevelRESTClientTool;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

/**
 * @author WIN10
 * @date 2021-07-14 16:14
 */
@Component
@RabbitListener(queues = "rabbit")
@Controller
public class ConsumerApi {

    @RabbitHandler
    @RequestMapping("getEs")
    @ResponseBody
    public String pushListener(String apiSup) {
        /*JSONObject json = JSON.parseObject(apiSup);
        String id = json.get("id").toString();*/
        GetResponse resp = ESHighLevelRESTClientTool.selectById("user", "1");
        assert resp != null;
        if (!resp.isExists()) {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("account","carl");
            matchQueryBuilder.fuzziness();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            //boolQuery.must(QueryBuilders.matchAllQuery());
            boolQuery.filter(QueryBuilders.matchQuery("account","car11l"));
            builder.query(boolQuery);
            SearchRequest searchRequest = ESHighLevelRESTClientTool.gentSearchRequest("user", builder);
            SearchResponse resp1 = ESHighLevelRESTClientTool.searchSyn(searchRequest);

            return Arrays.toString(resp1.getHits().getHits());
        }
        String account = resp.getSource().get("account").toString();
        System.out.println(account);

        return account;
    }

}
