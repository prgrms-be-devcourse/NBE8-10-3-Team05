package com.back.domain.welfare.center.center.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.api.center")
public record CenterApiProperties(String url, String key) {}
