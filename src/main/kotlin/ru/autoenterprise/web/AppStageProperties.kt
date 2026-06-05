package ru.autoenterprise.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.stage")
data class AppStageProperties(
    var current: String = "",
    var description: String = "",
)
