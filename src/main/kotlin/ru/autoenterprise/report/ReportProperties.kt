package ru.autoenterprise.report

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.reports")
data class ReportProperties(
    var maxRows: Int = 500,
    var timeoutSeconds: Int = 15,
)
