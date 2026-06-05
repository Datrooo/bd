package ru.autoenterprise

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.autoenterprise.report.ReportCatalog
import ru.autoenterprise.report.ReportDefinition
import ru.autoenterprise.report.ReportExecutionRequest
import ru.autoenterprise.report.ReportService

@SpringBootTest(
    properties = [
        "spring.config.import=",
        "spring.flyway.locations=classpath:db/migration,classpath:db/local",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class ReportModuleIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reportCatalog: ReportCatalog

    @Autowired
    private lateinit var reportService: ReportService

    @Test
    fun reportRegistryContainsAllDefinitionsAndResources() {
        val definitions = reportCatalog.all()

        assertThat(definitions).hasSize(29)
        assertThat(definitions.map { definition -> definition.code })
            .containsExactlyElementsOf((1..29).map { index -> "r%02d".format(index) })
        assertThat(definitions.map { definition -> definition.code }.toSet()).hasSize(29)

        definitions.forEach { definition ->
            assertThat(ClassPathResource(definition.sqlResource).exists())
                .describedAs("SQL resource for ${definition.code}")
                .isTrue()
        }
    }

    @Test
    fun reportsRequireAuthenticationButAreAvailableToRegularRoles() {
        mockMvc.perform(get("/reports"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login"))

        val viewerSession = loginAs("viewer", "viewer")
        mockMvc.perform(get("/reports").session(viewerSession))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Каталог отчетов")))
        mockMvc.perform(get("/reports/r01").session(viewerSession))
            .andExpect(status().isOk())

        val dispatcherSession = loginAs("dispatcher", "dispatcher")
        mockMvc.perform(get("/reports").session(dispatcherSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/reports/r08").session(dispatcherSession))
            .andExpect(status().isOk())

        val adminSession = loginAs("admin", "admin")
        mockMvc.perform(get("/reports").session(adminSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/reports/r29").session(adminSession))
            .andExpect(status().isOk())

        mockMvc.perform(get("/sql-console").session(viewerSession))
            .andExpect(status().isForbidden())
        mockMvc.perform(get("/sql-console").session(adminSession))
            .andExpect(status().isForbidden())
    }

    @Test
    fun parameterizedReportValidatesRequiredFields() {
        val viewerSession = loginAs("viewer", "viewer")

        mockMvc.perform(
            post("/reports/r08")
                .session(viewerSession)
                .with(csrf())
                .param("categoryName", "BUS")
                .param("startDate", "2026-04-01"),
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("request", "endDate"))
    }

    @Test
    fun representativeReportsExecuteAgainstSeedData() {
        val r01 = execute("r01")
        assertThat(r01.columns).contains("id", "inventory_number", "category", "current_mileage")
        assertThat(r01.rowCount).isEqualTo(6)

        val r04 = execute("r04", vehicleId = 1)
        assertThat(r04.columns).contains("personnel_number", "last_name", "assignment_type")
        assertThat(r04.rows).anySatisfy { row ->
            assertThat(row).contains("EMP-004", "Смирнов", "PRIMARY")
        }

        val aprilStart = LocalDate.parse("2026-04-01")
        val aprilEnd = LocalDate.parse("2026-04-30")

        val r08 = execute("r08", categoryName = "BUS", startDate = aprilStart, endDate = aprilEnd)
        assertThat(r08.rows.single()).contains("BUS", "356.00")

        val r10 = execute("r10", categoryName = "BUS", startDate = aprilStart, endDate = aprilEnd)
        assertThat(r10.rows.single()).contains("BUS", "1", "5000.00")

        val r21 = execute("r21", startDate = aprilStart, endDate = aprilEnd)
        assertThat(r21.columns).contains("acquisition_date", "acquisition_type", "vehicle_id")
        assertThat(r21.rows).isEmpty()

        val r29 = execute("r29", startDate = aprilStart, endDate = aprilEnd)
        assertThat(r29.columns).contains("route_number", "vehicles_count", "total_passengers", "total_revenue")
        assertThat(r29.rows).anySatisfy { row ->
            assertThat(row).contains("10", "1", "1790", "36300.00")
        }
    }

    @Test
    fun smokeTestAllReportsExecuteWithSeedBackedParameters() {
        reportCatalog.all().forEach { definition ->
            val result = reportService.execute(definition, smokeRequest(definition))
            assertThat(result.columns)
                .describedAs("Columns for ${definition.code}")
                .isNotEmpty()
        }
    }

    private fun execute(
        code: String,
        vehicleId: Long? = null,
        employeeId: Long? = null,
        categoryName: String? = null,
        brandName: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ) = reportService.execute(
        reportCatalog.find(code)!!,
        ReportExecutionRequest(
            code = code,
            vehicleId = vehicleId,
            employeeId = employeeId,
            categoryName = categoryName,
            brandName = brandName,
            startDate = startDate,
            endDate = endDate,
        ),
    )

    private fun smokeRequest(definition: ReportDefinition): ReportExecutionRequest =
        ReportExecutionRequest(
            code = definition.code,
            vehicleId = 1,
            employeeId = employeeIdFor(definition.code),
            categoryName = "BUS",
            brandName = "ЛиАЗ",
            startDate = LocalDate.parse("2026-04-01"),
            endDate = LocalDate.parse("2026-04-30"),
        )

    private fun employeeIdFor(code: String): Long =
        when (code) {
            "r23" -> 3
            "r24" -> 2
            "r25" -> 1
            "r26", "r27" -> 8
            else -> 1
        }

    private fun loginAs(username: String, password: String): MockHttpSession =
        mockMvc.perform(formLogin().user(username).password(password))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/dashboard"))
            .andExpect(authenticated().withUsername(username))
            .andReturn()
            .request
            .session as MockHttpSession

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
