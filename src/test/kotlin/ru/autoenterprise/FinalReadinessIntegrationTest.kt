package ru.autoenterprise

import jakarta.servlet.RequestDispatcher
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    properties = [
        "spring.config.import=",
        "spring.flyway.locations=classpath:db/migration,classpath:db/local",
        "app.security.show-dev-accounts=true",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class FinalReadinessIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun demoScenarioRequiresAuthenticationAndShowsRoleBasedGuidance() {
        mockMvc.perform(get("/demo-scenario"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login"))

        val viewerSession = loginAs("viewer", "viewer")

        mockMvc.perform(get("/demo-scenario").session(viewerSession))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Демо-сценарий проекта")))
            .andExpect(content().string(containsString("Read-only показ для наблюдателя")))
            .andExpect(content().string(containsString("viewer")))
    }

    @Test
    fun dashboardShowsFinalStageAndQuickDemoEntryPoint() {
        val adminSession = loginAs("admin", "admin")

        mockMvc.perform(get("/dashboard").session(adminSession))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Этап 7")))
            .andExpect(content().string(containsString("Открыть demo-сценарий")))
    }

    @Test
    fun authenticatedUserGetsFriendly404Page() {
        val viewerSession = loginAs("viewer", "viewer")

        mockMvc.perform(
            get("/error")
                .accept(MediaType.TEXT_HTML)
                .session(viewerSession)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing-route-for-readiness-test"),
        )
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Страница не найдена")))
            .andExpect(content().string(containsString("Вернуться на обзор")))
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
