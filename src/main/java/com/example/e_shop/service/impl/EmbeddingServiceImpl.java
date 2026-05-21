package com.example.e_shop.service.impl;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.e_shop.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文本嵌入服务实现 - 基于阿里云 DashScope text-embedding-v4 模型。
 * <p>
 * 输出 1024 维向量，API Key 由 DashScopeConfig 在启动时注入。
 * </p>
 */
@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    /** DashScope 嵌入模型名称 */
    private static final String MODEL = "text-embedding-v4";

    private final TextEmbedding textEmbedding = new TextEmbedding();

    /**
     * 为单段文本生成 embedding。
     *
     * @param text 输入文本
     * @return 1024 维 float 向量
     */
    @Override
    public float[] embed(String text) {
        List<float[]> results = embedBatch(Collections.singletonList(text));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 批量生成 embedding，每次调用最多处理 25 条文本。
     *
     * @param texts 输入文本列表
     * @return 向量列表
     */
    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();

        List<float[]> allEmbeddings = new ArrayList<>();
        // DashScope 建议单次不超过 25 条，分批处理
        int batchSize = 25;

        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
            try {
                TextEmbeddingParam param = TextEmbeddingParam.builder()
                        .model(MODEL)
                        .texts(batch)
                        .build();

                TextEmbeddingResult result = textEmbedding.call(param);
                List<com.alibaba.dashscope.embeddings.TextEmbeddingResultItem> items = result.getOutput().getEmbeddings();

                for (var item : items) {
                    List<Double> vector = item.getEmbedding();
                    float[] arr = new float[vector.size()];
                    for (int j = 0; j < vector.size(); j++) {
                        arr[j] = vector.get(j).floatValue();
                    }
                    allEmbeddings.add(arr);
                }

                // API 限流保护，每批次间隔 200ms
                if (i + batchSize < texts.size()) {
                    Thread.sleep(200);
                }
            } catch (NoApiKeyException e) {
                log.error("DashScope API Key 未配置", e);
                throw new RuntimeException("DashScope API Key 未配置", e);
            } catch (Exception e) {
                log.error("调用 DashScope 文本嵌入 API 失败", e);
                throw new RuntimeException("文本嵌入调用失败", e);
            }
        }

        return allEmbeddings;
    }
}
