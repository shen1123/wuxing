package com.carl.tu.conf;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="es")
public class EsConfig {

    /**
     * 运行环境
     */
    private String appEnv;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据
     */
    private List<Map<String,String>> data;

    public String getAppEnv() {
        return appEnv;
    }

    public void setAppEnv(String appEnv) {
        this.appEnv = appEnv;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    @Bean
    public void esConfigInit(){
        try {
            HttpHost[] httpHosts = new HttpHost[data.size()];
            int i = 0;
            for (Map<String, String> paramMap : data) {
                httpHosts[i++] = new HttpHost(paramMap.get("host"), Integer.parseInt(paramMap.get("port")), paramMap.get("scheme"));
            }
            RestClientBuilder client = RestClient.builder(httpHosts);
            if (StringUtils.isNoneBlank(username, password)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));  //es账号密码
                client.setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                });
                //设置超时时间
                client.setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(150000);
                    requestConfigBuilder.setSocketTimeout(150000);
                    requestConfigBuilder.setConnectionRequestTimeout(150000);
                    return requestConfigBuilder;
                });
            }
            ESHighLevelRESTClientTool.setClient( new RestHighLevelClient(client));
            ESHighLevelRESTClientTool.setAppEnv(appEnv);
            System.out.println("es启动成功");
        } catch (Exception e) {
            System.out.println("es启动失败");
            e.printStackTrace();
        } finally {

        }
    }

}
