package com.back.domain.welfare.center.center.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom.api.center")
data class CenterApiProperties(val url: String?, val key: String?)
