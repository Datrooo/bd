package ru.autoenterprise.web

import java.sql.SQLException
import org.springframework.dao.DataAccessException

fun dataAccessErrorMessage(
    exception: DataAccessException,
    fallback: String,
): String =
    generateSequence<Throwable>(exception) { cause -> cause.cause }
        .filterIsInstance<SQLException>()
        .firstNotNullOfOrNull(::postgresUserErrorMessage)
        ?: fallback

private fun postgresUserErrorMessage(exception: SQLException): String? {
    if (exception.sqlState != POSTGRES_RAISE_EXCEPTION_SQL_STATE) {
        return null
    }

    return exception.message
        ?.lineSequence()
        ?.firstOrNull()
        ?.removePrefix("ERROR:")
        ?.trim()
        ?.takeIf(String::isNotBlank)
}

private const val POSTGRES_RAISE_EXCEPTION_SQL_STATE = "P0001"
