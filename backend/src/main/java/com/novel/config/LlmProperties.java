package com.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String apiUrl;
    private String apiKey;
    private String model;
    private int maxTokens;
    private double temperature;
}
