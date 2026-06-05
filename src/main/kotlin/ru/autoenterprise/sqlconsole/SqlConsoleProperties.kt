package ru.autoenterprise.sqlconsole

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.sql-console")
data class SqlConsoleProperties(
    var maxRows: Int = 200,
    var timeoutSeconds: Int = 10,
    var datasource: SqlConsoleDatasourceProperties = SqlConsoleDatasourceProperties(),
)

data class SqlConsoleDatasourceProperties(
    var url: String = "",
    var username: String = "",
    var password: String = "",
    var driverClassName: String = "org.postgresql.Driver",
)
