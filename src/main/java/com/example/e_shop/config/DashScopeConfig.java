package com.example.e_shop.config;

import com.alibaba.dashscope.utils.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope 配置类。
 * <p>
 * 从 application.yaml 读取 dashscope.api-key，
 * 在 Bean 初始化时设置到 DashScope SDK 的全局常量中。
 * </p>
 */
@Setter
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    private String apiKey;

    @PostConstruct
    public void init() {
        Constants.apiKey = apiKey;
    }
}
