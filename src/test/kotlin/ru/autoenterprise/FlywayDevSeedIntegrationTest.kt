package ru.autoenterprise

import java.math.BigDecimal
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
        "spring.flyway.locations=classpath:db/migration,classpath:db/local",
    ],
)
@Testcontainers
class FlywayDevSeedIntegrationTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun devSeedPopulatesExpectedDataAndTriggers() {
        assertThat(count("role")).isEqualTo(6)
        assertThat(count("app_user")).isEqualTo(6)
        assertThat(count("user_role")).isEqualTo(6)
        assertThat(count("vehicle")).isEqualTo(6)
        assertThat(roleColumnType()).isEqualTo("app_role")

        assertThat(decimalValue("SELECT current_mileage FROM vehicle WHERE id = 1"))
            .isEqualByComparingTo(BigDecimal("356.00"))
        assertThat(decimalValue("SELECT current_mileage FROM vehicle WHERE id = 2"))
            .isEqualByComparingTo(BigDecimal("140.00"))
        assertThat(decimalValue("SELECT current_mileage FROM vehicle WHERE id = 5"))
            .isEqualByComparingTo(BigDecimal("95.00"))

        assertThat(decimalValue("SELECT total_cost FROM repair WHERE id = 1"))
            .isEqualByComparingTo(BigDecimal("38000.00"))
        assertThat(decimalValue("SELECT total_cost FROM repair WHERE id = 2"))
            .isEqualByComparingTo(BigDecimal("5000.00"))

        val componentStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM component WHERE id = 3",
            String::class.java,
        )
        assertThat(componentStatus).isEqualTo("INSTALLED")
    }

    private fun count(tableName: String): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $tableName", Long::class.java)!!

    private fun decimalValue(sql: String): BigDecimal =
        jdbcTemplate.queryForObject(sql, BigDecimal::class.java)!!

    private fun roleColumnType(): String =
        jdbcTemplate.queryForObject(
            """
            SELECT udt_name
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'role' AND column_name = 'name'
            """.trimIndent(),
            String::class.java,
        )!!

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
