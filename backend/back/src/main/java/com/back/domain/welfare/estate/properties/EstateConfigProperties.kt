package com.back.domain.welfare.estate.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom.api.estate")
data class EstateConfigProperties(val url: String, val key: String)
