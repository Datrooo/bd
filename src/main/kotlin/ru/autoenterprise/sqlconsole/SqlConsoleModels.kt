package ru.autoenterprise.sqlconsole

data class SqlConsoleRequestForm(
    var sql: String = "",
)

data class SqlConsoleExecutionResult(
    val columns: List<String>,
    val rows: List<List<String>>,
    val rowCount: Int,
    val truncated: Boolean,
    val executionTimeMs: Long,
) {
    val isEmpty: Boolean
        get() = rowCount == 0
}

class SqlConsoleQueryRejectedException(message: String) : RuntimeException(message)
