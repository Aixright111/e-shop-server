package com.example.e_shop.Scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.model.entity.Products;
import com.example.e_shop.model.enums.ProductCategory;
import com.example.e_shop.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品向量嵌入定时任务。
 * <p>
 * 每 10 分钟执行一次，扫描所有 beEmbedding = false 的商品，
 * 调用 DashScope text-embedding-v4 为其生成 1024 维向量并持久化，
 * 完成后将 beEmbedding 设为 true，避免重复消耗 token。
 * </p>
 */
@Slf4j
@Component
public class ProductEmbeddingScheduler {

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 每 10 分钟执行一次，只为 beEmbedding = false 的商品生成嵌入。
     * initialDelay = 1 分钟，避免应用刚启动时与其它任务冲突。
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 60000)
    public void embedProductNames() {
        // 只处理未嵌入的商品
        LambdaQueryWrapper<Products> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Products::isBeEmbedding, false);
        List<Products> products = productsMapper.selectList(wrapper);

        if (products.isEmpty()) {
            log.info("所有商品已完成嵌入，无需处理");
            return;
        }

        log.info("开始为 {} 个商品生成向量嵌入", products.size());

        int successCount = 0;
        for (Products product : products) {
            try {
                // 将商品名称 + 分类 + 描述拼接作为嵌入文本
                ProductCategory category = ProductCategory.of(product.getTypeId());
                String categoryName = category != null ? category.getDisplayName() : "";
                String text = String.format("%s %s %s",
                        product.getName() != null ? product.getName() : "",
                        categoryName,
                        product.getDescription() != null ? product.getDescription() : "").trim();
                float[] vector = embeddingService.embed(text);
                if (vector != null) {
                    product.setEmbedding(vector);
                    product.setBeEmbedding(true);
                    productsMapper.updateById(product);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("商品 {}（{}）嵌入失败: {}", product.getId(), product.getName(), e.getMessage());
            }
        }

        log.info("向量嵌入完成，成功: {}，失败: {}", successCount, products.size() - successCount);
    }
}
