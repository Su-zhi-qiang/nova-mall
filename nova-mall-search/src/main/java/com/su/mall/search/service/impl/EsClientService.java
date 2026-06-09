package com.su.mall.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EsClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsClientService.class);
    private static final String INDEX_NAME = "pms";

    private final RestHighLevelClient restHighLevelClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            createIndexIfNotExists();
        } catch (Exception e) {
            LOGGER.warn("索引初始化失败: {}", e.getMessage());
        }
    }

    public void createIndexIfNotExists() throws IOException {
        // 新版 ES 客户端必须指定 indices，否则抛出 "indices are mandatory"
        org.elasticsearch.action.admin.indices.get.GetIndexRequest existsRequest =
                new org.elasticsearch.action.admin.indices.get.GetIndexRequest().indices(INDEX_NAME);
        boolean exists = restHighLevelClient.indices().exists(existsRequest, RequestOptions.DEFAULT);

        if (!exists) {
            CreateIndexRequest createRequest = new CreateIndexRequest(INDEX_NAME);
            createRequest.source(getIndexMapping(), XContentType.JSON);
            CreateIndexResponse createResponse = restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
            LOGGER.info("索引 {} 创建结果: {}", INDEX_NAME, createResponse.isAcknowledged());
        } else {
            LOGGER.info("索引 {} 已存在", INDEX_NAME);
        }
    }

    private String getIndexMapping() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ObjectNode mappings = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();

        properties.put("id", createFieldNode("keyword"));
        properties.put("productSn", createFieldNode("keyword"));
        properties.put("brandId", createFieldNode("long"));
        properties.put("brandName", createFieldNode("keyword"));
        properties.put("productCategoryId", createFieldNode("long"));
        properties.put("productCategoryName", createFieldNode("keyword"));
        properties.put("pic", createFieldNode("keyword"));
        properties.putObject("name").put("type", "text").put("analyzer", "ik_max_word");
        properties.putObject("subTitle").put("type", "text").put("analyzer", "ik_max_word");
        properties.putObject("keywords").put("type", "text").put("analyzer", "ik_max_word");
        properties.put("price", createFieldNode("scaled_float").put("scaling_factor", 100));
        properties.put("sale", createFieldNode("integer"));
        properties.put("newStatus", createFieldNode("integer"));
        properties.put("recommandStatus", createFieldNode("integer"));
        properties.put("stock", createFieldNode("integer"));
        properties.put("promotionType", createFieldNode("integer"));
        properties.put("sort", createFieldNode("integer"));

        ObjectNode attrValueListNode = mapper.createObjectNode();
        attrValueListNode.put("type", "nested");
        ObjectNode attrValueListProps = mapper.createObjectNode();
        attrValueListProps.put("value", createFieldNode("keyword"));
        attrValueListProps.put("name", createFieldNode("keyword"));
        attrValueListProps.put("productAttributeId", createFieldNode("long"));
        attrValueListProps.put("type", createFieldNode("keyword"));
        attrValueListNode.set("properties", attrValueListProps);
        properties.set("attrValueList", attrValueListNode);

        mappings.set("properties", properties);
        root.set("mappings", mappings);

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            LOGGER.error("创建索引映射失败", e);
            return "{}";
        }
    }

    private ObjectNode createFieldNode(String type) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        return node;
    }

    public void indexDocument(String id, Map<String, Object> document) throws IOException {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        request.id(id);
        // ====================== 改这里 ======================
        request.source(document, "json");
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        LOGGER.debug("文档 {} 索引结果: {}", id, response.status());
    }

    public void bulkIndexDocuments(List<Map<String, Object>> documents) throws IOException {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        for (Map<String, Object> doc : documents) {
            Object id = doc.get("id");
            if (id != null) {
                indexDocument(String.valueOf(id), doc);
            }
        }
    }

    public void deleteDocument(String id) throws IOException {
        DeleteRequest request = new DeleteRequest(INDEX_NAME, id);
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        LOGGER.debug("文档 {} 删除结果: {}", id, response.status());
    }

    public SearchHits search(String keyword, Long brandId, Long productCategoryId, int from, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (keyword != null && !keyword.isEmpty()) {
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            shouldQuery.should(QueryBuilders.matchQuery("name", keyword).boost(10));
            shouldQuery.should(QueryBuilders.matchQuery("subTitle", keyword).boost(5));
            shouldQuery.should(QueryBuilders.matchQuery("keywords", keyword).boost(2));
            shouldQuery.minimumShouldMatch("1");
            boolQueryBuilder.must(shouldQuery);
        }

        if (brandId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", brandId));
        }
        if (productCategoryId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("productCategoryId", productCategoryId));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return response.getHits();
    }

    public SearchHits simpleSearch(String keyword, int from, int size) throws IOException {
        return search(keyword, null, null, from, size);
    }
}