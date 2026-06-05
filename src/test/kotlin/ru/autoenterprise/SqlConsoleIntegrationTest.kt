package ru.autoenterprise

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
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
class SqlConsoleIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun superadminCanOpenSqlConsoleAndExecuteReadOnlySelect() {
        val superadminSession = loginAs("superadmin", "superadmin")

        mockMvc.perform(get("/sql-console").session(superadminSession))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("SQL-консоль")))

        mockMvc.perform(
            post("/sql-console")
                .session(superadminSession)
                .with(csrf())
                .param("sql", "SELECT username, is_active FROM app_user ORDER BY id"),
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("executionResult"))
            .andExpect(content().string(containsString("username")))
            .andExpect(content().string(containsString("superadmin")))
    }

    @Test
    fun superadminCannotExecuteUpdateStatement() {
        val superadminSession = loginAs("superadmin", "superadmin")
        val beforeUsername = stringValue("SELECT username FROM app_user WHERE id = 1")

        mockMvc.perform(
            post("/sql-console")
                .session(superadminSession)
                .with(csrf())
                .param("sql", "UPDATE app_user SET username = 'hacked' WHERE id = 1"),
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("sqlConsole", "sql"))
            .andExpect(content().string(containsString("запрещенное ключевое слово")))

        val afterUsername = stringValue("SELECT username FROM app_user WHERE id = 1")
        org.assertj.core.api.Assertions.assertThat(afterUsername).isEqualTo(beforeUsername)
    }

    @Test
    fun superadminCannotExecuteMultipleStatements() {
        val superadminSession = loginAs("superadmin", "superadmin")

        mockMvc.perform(
            post("/sql-console")
                .session(superadminSession)
                .with(csrf())
                .param("sql", "SELECT 1; SELECT 2"),
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("sqlConsole", "sql"))
            .andExpect(content().string(containsString("Разрешен только один SQL statement.")))
    }

    @Test
    fun superadminCannotCallDisallowedFunctionsOrSystemNamespaces() {
        val superadminSession = loginAs("superadmin", "superadmin")

        mockMvc.perform(
            post("/sql-console")
                .session(superadminSession)
                .with(csrf())
                .param("sql", "SELECT pg_read_file('/etc/passwd')"),
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("sqlConsole", "sql"))
            .andExpect(content().string(containsString("без доступа к системным namespace PostgreSQL")))
    }

    private fun loginAs(username: String, password: String): MockHttpSession =
        mockMvc.perform(formLogin().user(username).password(password))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/dashboard"))
            .andExpect(authenticated().withUsername(username))
            .andReturn()
            .request
            .session as MockHttpSession

    private fun stringValue(sql: String): String =
        jdbcTemplate.queryForObject(sql, String::class.java)!!

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
