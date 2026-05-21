package com.example.e_shop.service;

import java.util.List;

/**
 * 文本嵌入（Embedding）服务接口。
 * <p>
 * 将商品名称等文本转为向量，用于语义搜索。
 * </p>
 */
public interface EmbeddingService {

    /**
     * 为单段文本生成向量嵌入。
     *
     * @param text 输入文本
     * @return 1024 维 float 向量
     */
    float[] embed(String text);

    /**
     * 为多段文本批量生成向量嵌入。
     *
     * @param texts 输入文本列表
     * @return 向量列表，顺序与输入一致
     */
    List<float[]> embedBatch(List<String> texts);
}
