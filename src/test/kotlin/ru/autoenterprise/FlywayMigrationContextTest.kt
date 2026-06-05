package ru.autoenterprise

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    properties = [
        "spring.config.import=",
        "spring.flyway.locations=classpath:db/migration",
    ],
)
@Testcontainers
class FlywayMigrationContextTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun contextLoadsAndAppliesBaseMigrations() {
        val migrationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
            Long::class.java,
        )
        val vehicleTableCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'vehicle'",
            Long::class.java,
        )
        val roleColumnType = jdbcTemplate.queryForObject(
            """
            SELECT udt_name
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'role' AND column_name = 'name'
            """.trimIndent(),
            String::class.java,
        )

        assertThat(migrationCount).isEqualTo(6)
        assertThat(vehicleTableCount).isEqualTo(1)
        assertThat(roleColumnType).isEqualTo("app_role")
    }

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
