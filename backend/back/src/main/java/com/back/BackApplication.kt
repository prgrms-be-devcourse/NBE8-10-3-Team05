package com.back

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaAuditing
class BackApplication

fun main(args: Array<String>) {
    runApplication<BackApplication>(*args)
}
