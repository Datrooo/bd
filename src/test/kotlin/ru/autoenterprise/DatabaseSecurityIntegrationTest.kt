package ru.autoenterprise

import java.sql.Timestamp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    properties = [
        "spring.config.import=",
        "spring.flyway.locations=classpath:db/migration,classpath:db/local",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class DatabaseSecurityIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun databaseUserCanLoginAndUpdateLastLoginTimestamp() {
        jdbcTemplate.update("UPDATE app_user SET last_login_at = NULL WHERE username = ?", "admin")
        assertThat(lastLoginAt("admin")).isNull()

        mockMvc.perform(formLogin().user("admin").password("admin"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/dashboard"))
            .andExpect(authenticated().withUsername("admin").withRoles("ADMIN"))

        assertThat(lastLoginAt("admin")).isNotNull()
    }

    @Test
    fun loginAcceptsMixedCaseUsernameButStoredUsernameRemainsNormalized() {
        mockMvc.perform(formLogin().user("AdMiN").password("admin"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/dashboard"))
            .andExpect(authenticated().withUsername("admin").withRoles("ADMIN"))

        val normalizedUsername = jdbcTemplate.queryForObject(
            "SELECT username FROM app_user WHERE username = ?",
            String::class.java,
            "admin",
        )
        assertThat(normalizedUsername).isEqualTo("admin")
    }

    @Test
    fun inactiveUserCannotAuthenticate() {
        val userId = jdbcTemplate.queryForObject(
            """
            INSERT INTO app_user (username, password_hash, employee_id, is_active, created_at, last_login_at)
            VALUES (?, ?, NULL, FALSE, CURRENT_TIMESTAMP, NULL)
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            "blocked-user",
            passwordEncoder.encode("blocked-password"),
        )

        jdbcTemplate.update(
            """
            INSERT INTO user_role (user_id, role_id)
            SELECT ?, id
            FROM role
            WHERE name = 'VIEWER'
            """.trimIndent(),
            userId,
        )

        mockMvc.perform(formLogin().user("blocked-user").password("blocked-password"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated())
    }

    @Test
    fun databaseRolesProtectUsersAndSqlConsoleRoutes() {
        val adminSession = loginAs("admin", "admin")
        mockMvc.perform(get("/users").session(adminSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/sql-console").session(adminSession))
            .andExpect(status().isForbidden())

        val viewerSession = loginAs("viewer", "viewer")
        mockMvc.perform(get("/users").session(viewerSession))
            .andExpect(status().isForbidden())
    }

    private fun loginAs(username: String, password: String): MockHttpSession =
        mockMvc.perform(formLogin().user(username).password(password))
            .andExpect(status().is3xxRedirection)
            .andExpect(authenticated().withUsername(username))
            .andReturn()
            .request
            .session as MockHttpSession

    private fun lastLoginAt(username: String): Timestamp? =
        jdbcTemplate.queryForObject(
            "SELECT last_login_at FROM app_user WHERE username = ?",
            Timestamp::class.java,
            username,
        )

    companion object {
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("auto_enterprise")
            .withUsername("postgres")
            .withPassword("postgres")

        @DynamicPropertySource
        @JvmStatic
        fun registerDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        }
    }
}
