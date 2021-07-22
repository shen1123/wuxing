package com.carl.tu.conf;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//参考文档
//https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-create-index.html

//Query build help
//https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-query-builders.html
//Aggregation build help
//https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-aggregation-builders.html


public class ESHighLevelRESTClientTool {

    private final static Logger logger = LoggerFactory.getLogger(ESHighLevelRESTClientTool.class);

    private static RestHighLevelClient client = null;

    private static String appEnv = null;

    private final static Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

    public static String getAppEnv() {
        return appEnv;
    }

    public static void setAppEnv(String appEnv) {
        ESHighLevelRESTClientTool.appEnv = appEnv;
    }

    public static void setClient(RestHighLevelClient client) {
        ESHighLevelRESTClientTool.client = client;
    }

    public static RestHighLevelClient getClient() {
        return client;
    }

    /**
     * 生成 创建索引
     *
     * @param index
     * @param mapping
     *
     * @return
     */
    public static CreateIndexRequest genCreateIndex(String index, Map<String, Object> mapping) {
        return genCreateIndex(index, mapping, 5, 1);
    }

    /**
     * 生成 创建索引
     *
     * @param index
     * @param mapping
     * @param shardsNum   分片个数
     * @param replicasNum 数据备份数
     *
     * @return
     */
    public static CreateIndexRequest genCreateIndex(String index, Map<String, Object> mapping, int shardsNum, int replicasNum) {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                        .put("index.number_of_shards", shardsNum)
                        .put("index.number_of_replicas", replicasNum)
                        );
        request.mapping(mapping);
        return request;
    }

    /**
     * 同步 创建索引
     *
     * @param request
     *
     * @return
     */
    public static CreateIndexResponse createSyn(CreateIndexRequest request) {
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步 创建索引
     *
     * @param request
     */
    public static void createAsyn(CreateIndexRequest request) {
        try {
            client.indices().createAsync(request, RequestOptions.DEFAULT, new ActionListener<CreateIndexResponse>() {
                @Override
                public void onResponse(CreateIndexResponse indexResponse) {

                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("创建失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GetIndexRequest genGetIndex(String index) {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
//        request.indicesOptions(indicesOptions);
        return request;
    }


    /**
     * 同步 检索索引
     *
     * @param indexRequest
     *
     * @return
     */
    public static boolean checkExist(GetIndexRequest indexRequest) {
        try {
            boolean exists = client.indices().exists(indexRequest, RequestOptions.DEFAULT);
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建index索引
     *
     * @param index
     * @param _id
     * @param jsonString
     *
     * @return
     */
    public static IndexRequest genInsertRequest(String index, String _id, String jsonString) {
        return new IndexRequest(index).id(_id).source(jsonString, XContentType.JSON);
    }

    /**
     * 创建index索引
     *
     * @param index
     * @param _id
     * @param paramMap
     *
     * @return
     */
    public static IndexRequest genInsertRequest(String index, String _id, Map<String, Object> paramMap) {
        return new IndexRequest(index).id(_id).source(paramMap);
    }

    /**
     * XContentBuilder builder = XContentFactory.jsonBuilder();
     * builder.startObject();
     * {
     * builder.field("user", "kimchy");
     * builder.field("postDate", new Date());
     * builder.field("message", "trying out Elasticsearch");
     * }
     * builder.endObject();
     *
     * @param index
     * @param _id
     * @param builder
     */
    public static IndexRequest genInsertRequest(String index, String _id, XContentBuilder builder) {
        IndexRequest request = new IndexRequest(index).id(_id).source(builder);
        return request;
    }

    /**
     * 特别处理 indexRequest
     * <p>
     * 参考网址 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-document-index.html
     *
     * @param request       IndexRequest
     * @param routing       路由
     * @param timeValue     超时等待 例如1s ,1m
     * @param refreshPolicy 刷新策略
     * @param version       版本
     * @param versionType   版本类型
     * @param opType        操作类型
     * @param pipeLine      索引文档之前要执行的接收管道的名称
     *
     * @return
     */
    public static IndexRequest specTreatIndexRequest(IndexRequest request, String routing, String timeValue,
                                                     WriteRequest.RefreshPolicy refreshPolicy, Long version,
                                                     VersionType versionType, DocWriteRequest.OpType opType,
                                                     String pipeLine) {
        if (StringUtils.isNotBlank(routing)) {
            request.routing(routing);
        }
        if (StringUtils.isNotBlank(timeValue)) {
            request.timeout(timeValue);
        }
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
        }
        if (version != null) {
            request.version(version);
        }
        if (versionType != null) {
            request.versionType(versionType);
        }
        if (opType != null) {
            request.opType(opType);
        }
        if (StringUtils.isNotBlank(pipeLine)) {
            request.setPipeline(pipeLine);
        }
        return request;
    }

    /**
     * 同步插入
     *
     * @param request
     *
     * @return
     */
    public static IndexResponse insertSyn(IndexRequest request) {
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            return indexResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步插入
     *
     * @param request
     */
    public static void insertAsyn(IndexRequest request) {
        try {
            client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {

                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("插入失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过 _id 查询
     *
     * @param index
     * @param _id
     *
     * @return
     */
    public static GetResponse selectById(String index, String _id) {
        try {
            GetRequest getRequest = new GetRequest(index);
            getRequest.id(_id);
            return client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询文档是存在(同步)
     *
     * @param index
     * @param _id
     *
     * @return
     */
    public static boolean existSyn(String index, String _id) {
        try {
            GetRequest getRequest = new GetRequest(index);
            getRequest.id(_id);
            getRequest.fetchSourceContext(new FetchSourceContext(false));
            getRequest.storedFields("_ none_");
            return client.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 生成 updateByQueryRequest
     * @param index
     * @param queryBuilder
     * @return
     */
    public static UpdateByQueryRequest genUpdateByQueryRequest(String index, QueryBuilder queryBuilder, Script script){
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        request.setQuery(queryBuilder);
        request.setConflicts("proceed");
        request.setScript(script);
        return request;
    }

    /**
     * 同步 条件更新
     * <p>
     * 参考 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-document-update-by-query.html
     *
     * @param updateByQueryRequest
     *
     * @return
     */
    public static BulkByScrollResponse updateByQuerySyn(UpdateByQueryRequest updateByQueryRequest) {
        try {
            BulkByScrollResponse bulkResponse = client.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
            return bulkResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步 条件更新
     * <p>
     * 参考 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-document-update-by-query.html
     *
     * @param updateByQueryRequest
     */
    public static void updateByQueryAsyn(UpdateByQueryRequest updateByQueryRequest) {
        try {
            client.updateByQueryAsync(updateByQueryRequest, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkResponse) {
                    logger.error("删除，${}，返回{}", updateByQueryRequest, bulkResponse);
                }

                @Override
                public void onFailure(Exception e) {
                    logger.error(">>>>>" + e.getMessage());
                    logger.error("删除" + updateByQueryRequest + "失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 生成 deleteByQueryRequest
     * @param index
     * @param queryBuilder
     * @return
     */
    public static DeleteByQueryRequest genDeleteByQueryRequest(String index, QueryBuilder queryBuilder){
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(queryBuilder);
        request.setConflicts("proceed");
        return request;
    }

    /**
     * 同步 条件删除
     * <p>
     * 参考 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-document-delete-by-query.html
     *
     * @param deleteByQueryRequest
     *
     * @return
     */
    public static BulkByScrollResponse deleteByQuerySyn(DeleteByQueryRequest deleteByQueryRequest) {
        try {
            BulkByScrollResponse bulkResponse = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
            return bulkResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步 条件删除
     * <p>
     * 参考 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-document-delete-by-query.html
     *
     * @param deleteByQueryRequest
     */
    public static void deleteByQueryAsyn(DeleteByQueryRequest deleteByQueryRequest) {
        try {
            client.deleteByQueryAsync(deleteByQueryRequest, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkResponse) {
                    logger.error("删除，${}，返回{}", deleteByQueryRequest, bulkResponse);
                }

                @Override
                public void onFailure(Exception e) {
                    logger.error(">>>>>" + e.getMessage());
                    logger.error("删除" + deleteByQueryRequest + "失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    /**
     * 删除索引(同步)
     *
     * @param index
     * @param _id
     *
     * @return
     */
    public static boolean deleteSyn(String index, String _id) {
        try {
            DeleteRequest request = new DeleteRequest(index).id(_id);
            request.timeout(TimeValue.timeValueMinutes(2));
            request.timeout("2m");
            DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
            return deleteResponse.getShardInfo().getFailed() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除索引(异步)
     *
     * @param index
     *
     * @return
     */
    public static void deleteAsyn(String index, String _id) {
        try {
            DeleteRequest request = new DeleteRequest(index).id(_id);
            request.timeout(TimeValue.timeValueMinutes(2));
            request.timeout("2m");
            client.deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse deleteResponse) {
                    logger.error("删除" + index + (deleteResponse.getShardInfo().getFailed() == 0 ? "成功" : "失败"));
                }

                @Override
                public void onFailure(Exception e) {
                    logger.error(">>>>>" + e.getMessage());
                    logger.error("删除" + index + "失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param index
     * @param _id
     * @param jsonString
     *
     * @return
     */
    public static UpdateRequest genUpdateRequest(String index, String _id, String jsonString) {
        return new UpdateRequest().index(index).id(_id).doc(jsonString, XContentType.JSON);
    }

    /**
     * @param index
     * @param _id
     * @param paramMap
     *
     * @return
     */
    public static UpdateRequest genUpdateRequest(String index, String _id, Map<String, Object> paramMap) {
        return new UpdateRequest().index(index).id(_id).doc(paramMap);
    }

    /**
     * XContentBuilder builder = XContentFactory.jsonBuilder（）;
     * builder.startObject（）;
     * {
     * builder.timeField（“updated”，new Date（））;
     * builder.field（“reason”，“每日更新”）;
     * }
     * builder.endObject（）;
     *
     * @param index
     * @param _id
     * @param builder
     *
     * @return
     */
    public static UpdateRequest genUpdateRequest(String index, String _id, XContentBuilder builder) {
        return new UpdateRequest().index(index).id(_id).doc(builder);
    }

    /**
     * @param index
     * @param _id
     * @param jsonString
     *
     * @return
     */
    public static UpdateRequest genUpsetRequest(String index, String _id, String jsonString) {
        UpdateRequest request = new UpdateRequest().index(index).id(_id);
        request.upsert(jsonString, XContentType.JSON);
        return request;
    }

    /**
     * @param index
     * @param _id
     * @param paramMap
     *
     * @return
     */
    public static UpdateRequest genUpsetRequest(String index, String _id, Map<String, Object> paramMap) {
        UpdateRequest request = new UpdateRequest().index(index).id(_id).upsert(paramMap);
        return request;
    }

    /**
     * @param index
     * @param _id
     * @param builder
     *
     * @return
     */
    public static UpdateRequest genUpsetRequest(String index, String _id, XContentBuilder builder) {
        UpdateRequest request = new UpdateRequest().index(index).id(_id).upsert(builder);
        return request;
    }

    /**
     * String index = updateResponse.getIndex();
     * String type = updateResponse.getType();
     * String id = updateResponse.getId();
     * long version = updateResponse.getVersion();
     * if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
     * <p>
     * } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
     * <p>
     * } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
     * <p>
     * } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
     * <p>
     * }
     *
     * @param request
     *
     * @return
     */
    public static UpdateResponse updateSyn(UpdateRequest request) {
        try {
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            return updateResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * @param request
     */
    public static void updateAsyn(UpdateRequest request) {
        try {
            client.updateAsync(request, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
                @Override
                public void onResponse(UpdateResponse updateResponse) {
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("更新失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param indexRequestList
     * @param deleteRequestList
     * @param updateRequestList
     *
     * @return
     */
    public BulkRequest genBulkRequest(List<IndexRequest> indexRequestList,
                                      List<DeleteRequest> deleteRequestList,
                                      List<UpdateRequest> updateRequestList) {
        BulkRequest request = new BulkRequest();
        for (IndexRequest indexRequest : indexRequestList) {
            request.add(indexRequest);
        }
        for (DeleteRequest deleteRequest : deleteRequestList) {
            request.add(deleteRequest);
        }
        for (UpdateRequest updateRequest : updateRequestList) {
            request.add(updateRequest);
        }
        return request;
    }

    /**
     * for (BulkItemResponse bulkItemResponse : bulkResponse) {
     * DocWriteResponse itemResponse = bulkItemResponse.getResponse();
     * <p>
     * if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
     * || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
     * IndexResponse indexResponse = (IndexResponse) itemResponse;
     * <p>
     * } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
     * UpdateResponse updateResponse = (UpdateResponse) itemResponse;
     * <p>
     * } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
     * DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
     * }
     * }
     *
     * @param request
     *
     * @return
     */
    public static BulkResponse bulkSyn(BulkRequest request) {
        try {
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            return bulkResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * @param request
     */
    public static void bulkAsyn(BulkRequest request) {
        try {
            client.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkResponse) {

                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("bulk失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * //Create aSearchSourceBuilderwith default options.
     * SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/6.1/java-rest-high-search.html#java-rest-high-document-search-request
     *
     * @param index
     * @param searchSourceBuilder
     */
    public static SearchRequest gentSearchRequest(String index, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    /**
     * @param searchRequest
     *
     * @return
     */
    public static SearchResponse searchSyn(SearchRequest searchRequest) {
        try {
            return client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param searchRequest
     */
    public static void searchAsyn(SearchRequest searchRequest) {
        try {
            client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("查询失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ClusterName clusterName = response.getClusterName();  //Retrieve the name of the cluster as a ClusterName
     * String clusterUuid = response.getClusterUuid();  //Retrieve the unique identifier of the cluster
     * String nodeName = response.getNodeName();  //Retrieve the name of the node the request has been executed on
     * Version version = response.getVersion(); //Retrieve the version of the node the request has been executed on
     * Build build = response.getBuild();  //Retrieve the build information of the node the request has been executed on
     * <p>
     * <p>
     * 获取信息
     *
     * @return
     */
    public static MainResponse getInfo() {
        MainResponse response;
        try {
            response = client.info(RequestOptions.DEFAULT);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 滚动查询初始化
     *
     * @param index
     * @param searchSourceBuilder
     *
     * @return
     */
    public static SearchResponse scrollSearchInit(String index, SearchSourceBuilder searchSourceBuilder) {
        try {
            SearchRequest searchRequest = new SearchRequest(index);
            searchRequest.source(searchSourceBuilder);
            searchRequest.scroll(scroll);
            return client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 同步滚动查询
     *
     * @param scrollId
     *
     * @return
     */
    public static SearchResponse scrollSearchSyn(String scrollId) {
        try {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            return client.scroll(scrollRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//		scrollId = searchScrollResponse.getScrollId();
//		SearchHits hits = searchScrollResponse.getHits();
    }

    /**
     * 异步滚动查询
     *
     * @param scrollId
     */
    public static void scrollSearchAsyn(String scrollId) {
        try {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            client.scrollAsync(scrollRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    logger.info("scollSearch 成功");
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    logger.error("scollSearch 失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 清除滚动请求(类似于gc)
     *
     * @param scrollIds
     *
     * @return
     */
    public static ClearScrollRequest genClearScollSearchReq(List<String> scrollIds) {
        ClearScrollRequest request = new ClearScrollRequest();
        request.setScrollIds(scrollIds);
        return request;
    }


    /**
     * 同步清除
     *
     * @param request
     */
    public static ClearScrollResponse clearScollSearchSyn(ClearScrollRequest request) {
        try {
            return client.clearScroll(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步清除
     *
     * @param request
     */
    public static void clearScollSearchAsyn(ClearScrollRequest request) {
        try {
            client.clearScrollAsync(request, RequestOptions.DEFAULT, new ActionListener<ClearScrollResponse>() {
                @Override
                public void onResponse(ClearScrollResponse clearScrollResponse) {
                    logger.info("清除成功");
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
