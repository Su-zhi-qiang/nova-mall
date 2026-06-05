package com.su.mall.search.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.su.mall.search.dao.EsProductDao;
import com.su.mall.search.domain.EsProduct;
import com.su.mall.search.domain.EsProductRelatedInfo;
import com.su.mall.search.service.EsProductService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索商品管理Service实现类
 * 使用RestHighLevelClient连接Elasticsearch 7.17.3
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class EsProductServiceImpl implements EsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);

    private final EsProductDao productDao;

    private final EsClientService esClientService;

    @Override
    public int importAll() {
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        if (esProductList.isEmpty()) {
            return 0;
        }

        try {
            List<Map<String, Object>> documents = esProductList.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            esClientService.bulkIndexDocuments(documents);
            return documents.size();
        } catch (IOException e) {
            LOGGER.error("批量索引失败", e);
            return 0;
        }
    }

    @Override
    public void delete(Long id) {
        try {
            esClientService.deleteDocument(String.valueOf(id));
        } catch (IOException e) {
            LOGGER.error("删除文档失败: {}", id, e);
        }
    }

    @Override
    public EsProduct create(Long id) {
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (!esProductList.isEmpty()) {
            EsProduct esProduct = esProductList.get(0);
            try {
                Map<String, Object> document = convertToMap(esProduct);
                esClientService.indexDocument(String.valueOf(esProduct.getId()), document);
            } catch (IOException e) {
                LOGGER.error("索引文档失败: {}", id, e);
            }
            return esProduct;
        }
        return null;
    }

    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            for (Long id : ids) {
                try {
                    esClientService.deleteDocument(String.valueOf(id));
                } catch (IOException e) {
                    LOGGER.error("删除文档失败: {}", id, e);
                }
            }
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        try {
            int from = pageNum * pageSize;
            SearchHits searchHits = esClientService.simpleSearch(keyword, from, pageSize);

            List<EsProduct> products = new ArrayList<>();
            for (SearchHit hit : searchHits.getHits()) {
                EsProduct product = convertFromMap(hit.getSourceAsMap());
                products.add(product);
            }
            // ES7取值：value是成员变量，无括号；显式泛型消除无法推断类型
            long total = searchHits.getTotalHits().value;
            return new PageImpl<EsProduct>(products, pageable, total);
        } catch (IOException e) {
            LOGGER.error("搜索失败", e);
            return new PageImpl<EsProduct>(ListUtil.empty(), pageable, 0);
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        try {
            int from = pageNum * pageSize;
            SearchHits searchHits = esClientService.search(keyword, brandId, productCategoryId, from, pageSize);

            List<EsProduct> products = new ArrayList<>();
            for (SearchHit hit : searchHits.getHits()) {
                EsProduct product = convertFromMap(hit.getSourceAsMap());
                products.add(product);
            }
            long total = searchHits.getTotalHits().value;
            return new PageImpl<EsProduct>(products, pageable, total);
        } catch (IOException e) {
            LOGGER.error("搜索失败", e);
            return new PageImpl<EsProduct>(ListUtil.empty(), pageable, 0);
        }
    }

    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);

        if (!esProductList.isEmpty()) {
            EsProduct esProduct = esProductList.get(0);
            return search(esProduct.getName(), esProduct.getBrandId(),
                    esProduct.getProductCategoryId(), pageNum, pageSize, null);
        }
        return new PageImpl<EsProduct>(ListUtil.empty(), pageable, 0);
    }

    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        EsProductRelatedInfo productRelatedInfo = new EsProductRelatedInfo();
        try {
            SearchHits searchHits = esClientService.simpleSearch(keyword, 0, 100);

            Set<String> brandNames = new HashSet<>();
            Set<String> categoryNames = new HashSet<>();
            List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();

            for (SearchHit hit : searchHits.getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                if (source.containsKey("brandName")) {
                    brandNames.add(String.valueOf(source.get("brandName")));
                }
                if (source.containsKey("productCategoryName")) {
                    categoryNames.add(String.valueOf(source.get("productCategoryName")));
                }
                if (source.containsKey("attrValueList")) {
                    Object attrValueListObj = source.get("attrValueList");
                    if (attrValueListObj instanceof List<?>) {
                        List<?> attrValues = (List<?>) attrValueListObj;
                        for (Object attrObj : attrValues) {
                            if (attrObj instanceof Map<?, ?>) {
                                Map<?, ?> attrMap = (Map<?, ?>) attrObj;
                                EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
                                if (attrMap.get("productAttributeId") != null) {
                                    attr.setAttrId(Long.valueOf(attrMap.get("productAttributeId").toString()));
                                }
                                if (attrMap.get("name") != null) {
                                    attr.setAttrName(String.valueOf(attrMap.get("name")));
                                }
                                List<String> values = new ArrayList<>();
                                if (attrMap.get("value") != null) {
                                    values.add(String.valueOf(attrMap.get("value")));
                                }
                                attr.setAttrValues(values);
                                attrList.add(attr);
                            }
                        }
                    }
                }
            }
            productRelatedInfo.setBrandNames(new ArrayList<>(brandNames));
            productRelatedInfo.setProductCategoryNames(new ArrayList<>(categoryNames));
            productRelatedInfo.setProductAttrs(attrList);
        } catch (IOException e) {
            LOGGER.error("搜索相关信息失败", e);
        }
        return productRelatedInfo;
    }

    /**
     * 将EsProduct转换为Map
     */
    private Map<String, Object> convertToMap(EsProduct product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        if (product.getProductSn() != null) map.put("productSn", product.getProductSn());
        if (product.getBrandId() != null) map.put("brandId", product.getBrandId());
        if (product.getBrandName() != null) map.put("brandName", product.getBrandName());
        if (product.getProductCategoryId() != null) map.put("productCategoryId", product.getProductCategoryId());
        if (product.getProductCategoryName() != null) map.put("productCategoryName", product.getProductCategoryName());
        if (product.getPic() != null) map.put("pic", product.getPic());
        if (product.getName() != null) map.put("name", product.getName());
        if (product.getSubTitle() != null) map.put("subTitle", product.getSubTitle());
        if (product.getKeywords() != null) map.put("keywords", product.getKeywords());
        if (product.getPrice() != null) map.put("price", product.getPrice());
        if (product.getSale() != null) map.put("sale", product.getSale());
        if (product.getNewStatus() != null) map.put("newStatus", product.getNewStatus());
        if (product.getRecommandStatus() != null) map.put("recommandStatus", product.getRecommandStatus());
        if (product.getStock() != null) map.put("stock", product.getStock());
        if (product.getPromotionType() != null) map.put("promotionType", product.getPromotionType());
        if (product.getSort() != null) map.put("sort", product.getSort());
        if (product.getAttrValueList() != null) {
            List<Map<String, Object>> attrMaps = product.getAttrValueList().stream()
                    .map(attr -> {
                        Map<String, Object> attrMap = new HashMap<>();
                        attrMap.put("value", attr.getValue());
                        attrMap.put("name", attr.getName());
                        if (attr.getProductAttributeId() != null) {
                            attrMap.put("productAttributeId", attr.getProductAttributeId());
                        }
                        if (attr.getType() != null) {
                            attrMap.put("type", attr.getType());
                        }
                        return attrMap;
                    })
                    .collect(Collectors.toList());
            map.put("attrValueList", attrMaps);
        }
        return map;
    }

    /**
     * 将Map转换为EsProduct
     */
    private EsProduct convertFromMap(Map<String, Object> map) {
        EsProduct product = new EsProduct();
        if (map.get("id") != null) {
            product.setId(Long.valueOf(map.get("id").toString()));
        }
        if (map.get("productSn") != null) product.setProductSn(String.valueOf(map.get("productSn")));
        if (map.get("brandId") != null) product.setBrandId(Long.valueOf(map.get("brandId").toString()));
        if (map.get("brandName") != null) product.setBrandName(String.valueOf(map.get("brandName")));
        if (map.get("productCategoryId") != null) product.setProductCategoryId(Long.valueOf(map.get("productCategoryId").toString()));
        if (map.get("productCategoryName") != null) product.setProductCategoryName(String.valueOf(map.get("productCategoryName")));
        if (map.get("pic") != null) product.setPic(String.valueOf(map.get("pic")));
        if (map.get("name") != null) product.setName(String.valueOf(map.get("name")));
        if (map.get("subTitle") != null) product.setSubTitle(String.valueOf(map.get("subTitle")));
        if (map.get("keywords") != null) product.setKeywords(String.valueOf(map.get("keywords")));
        if (map.get("price") != null) product.setPrice(new BigDecimal(map.get("price").toString()));
        if (map.get("sale") != null) product.setSale(Integer.valueOf(map.get("sale").toString()));
        if (map.get("newStatus") != null) product.setNewStatus(Integer.valueOf(map.get("newStatus").toString()));
        if (map.get("recommandStatus") != null) product.setRecommandStatus(Integer.valueOf(map.get("recommandStatus").toString()));
        if (map.get("stock") != null) product.setStock(Integer.valueOf(map.get("stock").toString()));
        if (map.get("promotionType") != null) product.setPromotionType(Integer.valueOf(map.get("promotionType").toString()));
        if (map.get("sort") != null) product.setSort(Integer.valueOf(map.get("sort").toString()));
        return product;
    }
}