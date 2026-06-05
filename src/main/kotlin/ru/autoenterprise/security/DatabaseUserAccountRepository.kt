package ru.autoenterprise.security

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

data class DatabaseUserAccount(
    val username: String,
    val passwordHash: String,
    val active: Boolean,
    val roles: List<AppRole>,
)

@Repository
class DatabaseUserAccountRepository(
    private val jdbcTemplate: JdbcTemplate,
) {

    fun findByUsername(username: String): DatabaseUserAccount? =
        try {
            jdbcTemplate.queryForObject(FIND_BY_USERNAME_SQL, accountRowMapper, username)
        } catch (_: EmptyResultDataAccessException) {
            null
        }

    fun updateLastLogin(username: String) {
        jdbcTemplate.update(UPDATE_LAST_LOGIN_SQL, username)
    }

    private val accountRowMapper = RowMapper<DatabaseUserAccount> { rs, _ ->
        val roles = (rs.getArray("role_names")?.array as? Array<*>)
            ?.filterIsInstance<String>()
            ?.map(AppRole::fromRoleName)
            .orEmpty()

        DatabaseUserAccount(
            username = rs.getString("username"),
            passwordHash = rs.getString("password_hash"),
            active = rs.getBoolean("is_active"),
            roles = roles,
        )
    }

    companion object {
        private const val FIND_BY_USERNAME_SQL = """
            SELECT
                u.username,
                u.password_hash,
                u.is_active,
                ARRAY_REMOVE(ARRAY_AGG(r.name::text ORDER BY r.name), NULL) AS role_names
            FROM app_user u
            LEFT JOIN user_role ur ON ur.user_id = u.id
            LEFT JOIN role r ON r.id = ur.role_id
            WHERE u.username = LOWER(BTRIM(?))
            GROUP BY u.id, u.username, u.password_hash, u.is_active
        """

        private const val UPDATE_LAST_LOGIN_SQL = """
            UPDATE app_user
            SET last_login_at = CURRENT_TIMESTAMP
            WHERE username = LOWER(BTRIM(?))
        """
    }
}
