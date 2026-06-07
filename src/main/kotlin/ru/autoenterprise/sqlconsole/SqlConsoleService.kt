package ru.autoenterprise.sqlconsole

import com.zaxxer.hikari.HikariDataSource
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.SQLException
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.stereotype.Service

@Service
class SqlConsoleService(
    private val dataSource: DataSource,
    private val sqlConsoleProperties: SqlConsoleProperties,
) {

    private val sqlConsoleDataSource: DataSource = createSqlConsoleDataSource()

    fun execute(sql: String, username: String): SqlConsoleExecutionResult {
        val safeSql = validateAndNormalize(sql)
        val startedAt = System.nanoTime()

        logger.info("SQL console query accepted for user={}: {}", username, preview(safeSql))

        return try {
            sqlConsoleDataSource.connection.use { connection ->
                val maxRows = sqlConsoleProperties.maxRows.coerceAtLeast(1)
                val fetchSize = minOf(defaultFetchSize, maxRows)

                connection.autoCommit = false
                connection.isReadOnly = true

                try {
                    connection.createStatement().use { sessionStatement ->
                        sessionStatement.execute("SET LOCAL search_path = public")
                        sessionStatement.execute("SET LOCAL statement_timeout = '${sqlConsoleProperties.timeoutSeconds.coerceAtLeast(1)}s'")
                    }

                    connection.prepareStatement(safeSql).use { statement ->
                        statement.maxRows = maxRows + 1
                        statement.fetchSize = fetchSize

                        statement.executeQuery().use { resultSet ->
                            val metadata = resultSet.metaData
                            val columns = (1..metadata.columnCount).map { index -> metadata.getColumnLabel(index) }
                            val rows = mutableListOf<List<String>>()
                            var truncated = false

                            while (resultSet.next()) {
                                if (rows.size == maxRows) {
                                    truncated = true
                                    break
                                }

                                rows += columns.indices.map { index ->
                                    formatValue(resultSet.getObject(index + 1))
                                }
                            }

                            SqlConsoleExecutionResult(
                                columns = columns,
                                rows = rows,
                                rowCount = rows.size,
                                truncated = truncated,
                                executionTimeMs = elapsedMs(startedAt),
                            )
                        }
                    }
                } finally {
                    connection.rollback()
                }
            }
        } catch (ex: SQLException) {
            logger.warn("SQL console query failed for user={}: {}", username, preview(safeSql), ex)
            throw ex
        }
    }

    private fun validateAndNormalize(sql: String): String {
        val normalized = sql.replace(Regex(";\\s*$"), "").trim()
        if (normalized.isBlank()) {
            throw SqlConsoleQueryRejectedException("Введите SQL-запрос.")
        }

        val sanitized = sanitizeSql(normalized)
        if (sanitized.contains(';')) {
            throw SqlConsoleQueryRejectedException("Разрешен только один SQL statement.")
        }

        val statementStart = sanitized.trimStart()

        forbiddenKeywords.firstOrNull { keyword ->
            Regex("\\b$keyword\\b").containsMatchIn(sanitized)
        }?.let { keyword ->
            throw SqlConsoleQueryRejectedException("Запрос отклонен политикой безопасности: найдено запрещенное ключевое слово `$keyword`.")
        }

        if (systemNamespaceRegex.containsMatchIn(sanitized)) {
            throw SqlConsoleQueryRejectedException("SQL-консоль работает только с прикладной схемой public без доступа к системным namespace PostgreSQL.")
        }

        val disallowedFunctions = extractCallLikeTokens(sanitized)
            .filterNot(ignoredCallLikeKeywords::contains)
            .filterNot(allowedFunctions::contains)
            .distinct()
        if (disallowedFunctions.isNotEmpty()) {
            throw SqlConsoleQueryRejectedException(
                "Разрешены только безопасные SQL-функции. Обнаружены недопустимые вызовы: ${disallowedFunctions.joinToString(", ")}.",
            )
        }

        val disallowedTables = extractReferencedTables(sanitized)
            .filterNot(allowedTables::contains)
            .distinct()
        if (disallowedTables.isNotEmpty()) {
            throw SqlConsoleQueryRejectedException(
                "SQL-консоль ограничена прикладными таблицами. Обнаружены недопустимые источники данных: ${disallowedTables.joinToString(", ")}.",
            )
        }

        if (!selectStatementStartRegex.containsMatchIn(statementStart)) {
            throw SqlConsoleQueryRejectedException("Разрешен только один read-only SELECT-запрос.")
        }

        return normalized
    }

    private fun sanitizeSql(sql: String): String =
        stripComments(stripQuotedContent(sql)).lowercase()

    private fun stripQuotedContent(sql: String): String =
        sql
            .replace(singleQuotedLiteralRegex, "''")
            .replace(doubleQuotedIdentifierRegex, "\"\"")
            .replace(dollarQuotedLiteralRegex, " ")

    private fun stripComments(sql: String): String =
        sql
            .replace(blockCommentRegex, " ")
            .replace(lineCommentRegex, " ")

    private fun formatValue(value: Any?): String =
        when (value) {
            null -> "—"
            is BigDecimal -> value.setScale(2, RoundingMode.HALF_UP).toPlainString()
            else -> value.toString()
        }

    private fun elapsedMs(startedAt: Long): Long =
        ((System.nanoTime() - startedAt) / 1_000_000).coerceAtLeast(0)

    private fun preview(sql: String): String =
        sql.replace(Regex("\\s+"), " ").take(maxPreviewLength)

    private fun createSqlConsoleDataSource(): DataSource {
        val datasourceProperties = sqlConsoleProperties.datasource
        if (datasourceProperties.url.isBlank() || datasourceProperties.username.isBlank()) {
            logger.warn("SQL console datasource is not configured separately; falling back to the primary datasource.")
            return dataSource
        }

        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .driverClassName(datasourceProperties.driverClassName)
            .url(datasourceProperties.url)
            .username(datasourceProperties.username)
            .password(datasourceProperties.password)
            .build()
    }

    private fun extractReferencedTables(sql: String): List<String> =
        tableReferenceRegex.findAll(sql)
            .map { match -> match.groupValues[1] }
            .toList()

    private fun extractCallLikeTokens(sql: String): List<String> =
        callLikeTokenRegex.findAll(sql)
            .map { match -> match.groupValues[1] }
            .toList()

    companion object {
        private val logger = LoggerFactory.getLogger(SqlConsoleService::class.java)

        private const val defaultFetchSize = 100
        private const val maxPreviewLength = 180

        private val forbiddenKeywords = listOf(
            "insert",
            "update",
            "delete",
            "merge",
            "alter",
            "drop",
            "truncate",
            "create",
            "grant",
            "revoke",
            "comment",
            "copy",
            "call",
            "do",
            "vacuum",
            "reindex",
            "cluster",
            "analyze",
            "refresh",
            "set",
            "reset",
            "begin",
            "commit",
            "rollback",
            "savepoint",
            "lock",
            "prepare",
            "execute",
            "listen",
            "unlisten",
            "notify",
            "with",
        )

        private val allowedFunctions = setOf(
            "abs",
            "array_agg",
            "avg",
            "btrim",
            "coalesce",
            "concat",
            "concat_ws",
            "count",
            "date_part",
            "date_trunc",
            "dense_rank",
            "extract",
            "greatest",
            "json_agg",
            "jsonb_agg",
            "lag",
            "lead",
            "least",
            "length",
            "lower",
            "ltrim",
            "max",
            "min",
            "nullif",
            "rank",
            "round",
            "row_number",
            "rtrim",
            "string_agg",
            "substring",
            "sum",
            "to_char",
            "to_date",
            "to_timestamp",
            "trim",
            "upper",
        )

        private val ignoredCallLikeKeywords = setOf(
            "case",
            "exists",
            "filter",
            "in",
            "over",
            "partition",
            "values",
        )

        private val allowedTables = setOf(
            "app_user",
            "brigade",
            "component",
            "employee",
            "employee_brigade_assignment",
            "flyway_schema_history",
            "garage_object",
            "repair",
            "repair_type",
            "repair_work",
            "role",
            "route",
            "route_vehicle_assignment",
            "section",
            "transportation_record",
            "user_role",
            "vehicle",
            "vehicle_acquisition",
            "vehicle_category",
            "vehicle_component_history",
            "vehicle_disposal",
            "vehicle_driver_assignment",
            "vehicle_location_history",
            "workshop",
        )

        private val singleQuotedLiteralRegex = Regex("'(?:''|[^'])*'")
        private val doubleQuotedIdentifierRegex = Regex("\"(?:\"\"|[^\"])*\"")
        private val dollarQuotedLiteralRegex = Regex("(?s)\\$[^$]*\\$.*?\\$[^$]*\\$")
        private val blockCommentRegex = Regex("(?s)/\\*.*?\\*/")
        private val lineCommentRegex = Regex("(?m)--.*?$")
        private val systemNamespaceRegex = Regex("\\b(?:information_schema|pg_catalog|pg_[a-z0-9_]+)\\b")
        private val selectStatementStartRegex = Regex("^select\\b")
        private val tableReferenceRegex = Regex("\\b(?:from|join)\\s+(?:only\\s+)?(?:public\\.)?([a-z_][a-z0-9_]*)\\b")
        private val callLikeTokenRegex = Regex("\\b([a-z_][a-z0-9_]*)\\s*\\(")
    }
}
