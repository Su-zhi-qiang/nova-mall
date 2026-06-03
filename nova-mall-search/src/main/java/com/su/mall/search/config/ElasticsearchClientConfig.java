package com.su.mall.search.config;

import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Elasticsearch客户端配置
 * 使用RestHighLevelClient连接Elasticsearch 7.17.3
 */
@Configuration
public class ElasticsearchClientConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    private RestHighLevelClient restHighLevelClient;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 解析URL
        String host = elasticsearchUrl.replace("http://", "").replace("https://", "");
        String[] parts = host.split(":");
        String hostname = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;

        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, "http"))
                        .setRequestConfigCallback(requestConfigBuilder ->
                                requestConfigBuilder
                                        .setConnectTimeout(5000)
                                        .setSocketTimeout(60000)
                        )
        );

        return restHighLevelClient;
    }

    @PreDestroy
    public void closeClient() {
        if (restHighLevelClient != null) {
            try {
                restHighLevelClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
