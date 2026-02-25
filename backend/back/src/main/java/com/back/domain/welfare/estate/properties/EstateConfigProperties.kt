package com.back.domain.welfare.estate.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.api.estate")
public record EstateConfigProperties(String url, String key) {}
