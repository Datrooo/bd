package ru.autoenterprise

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
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
class CrudStageFourIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun viewerCanOpenCrudListsButCannotOpenProtectedForms() {
        val viewerSession = loginAs("viewer", "viewer")

        mockMvc.perform(get("/vehicles").session(viewerSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/employees").session(viewerSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/routes").session(viewerSession))
            .andExpect(status().isOk())
        mockMvc.perform(get("/vehicle-categories").session(viewerSession))
            .andExpect(status().isOk())

        mockMvc.perform(get("/vehicles/new").session(viewerSession))
            .andExpect(status().isForbidden())
        mockMvc.perform(get("/employees/new").session(viewerSession))
            .andExpect(status().isForbidden())
        mockMvc.perform(get("/routes/new").session(viewerSession))
            .andExpect(status().isForbidden())
        mockMvc.perform(get("/users/new").session(viewerSession))
            .andExpect(status().isForbidden())
    }

    @Test
    fun adminCanCreateVehicleCategoryAndVehicle() {
        val adminSession = loginAs("admin", "admin")

        mockMvc.perform(
            post("/vehicle-categories")
                .session(adminSession)
                .with(csrf())
                .param("name", "Stage4 Category")
                .param("description", "Category created from integration test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicle-categories"))

        val categoryId = longValue(
            "SELECT id FROM vehicle_category WHERE name = ?",
            "Stage4 Category",
        )

        mockMvc.perform(
            post("/vehicles")
                .session(adminSession)
                .with(csrf())
                .param("inventoryNumber", "STAGE4-INV-001")
                .param("registrationNumber", "A444AA54")
                .param("categoryId", categoryId.toString())
                .param("brandName", "PAZ")
                .param("modelName", "3204")
                .param("manufactureYear", "2024")
                .param("currentMileage", "12.50")
                .param("status", "ACTIVE")
                .param("notes", "Created from stage 4 integration test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicles"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle WHERE inventory_number = ? AND category_id = ?",
                "STAGE4-INV-001",
                categoryId,
            ),
        ).isEqualTo(1)
    }

    @Test
    fun hrCanCreateEmployee() {
        val hrSession = loginAs("hr", "hr")

        mockMvc.perform(
            post("/employees")
                .session(hrSession)
                .with(csrf())
                .param("personnelNumber", "STAGE4-EMP-001")
                .param("lastName", "Тестов")
                .param("firstName", "Кадровик")
                .param("hireDate", "2026-04-27")
                .param("position", "Инспектор по кадрам")
                .param("status", "ACTIVE")
                .param("email", "stage4-hr@example.com"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/employees"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM employee WHERE personnel_number = ?",
                "STAGE4-EMP-001",
            ),
        ).isEqualTo(1)
    }

    @Test
    fun dispatcherCanCreateRoute() {
        val dispatcherSession = loginAs("dispatcher", "dispatcher")

        mockMvc.perform(
            post("/routes")
                .session(dispatcherSession)
                .with(csrf())
                .param("routeNumber", "S4-77")
                .param("name", "Тестовый маршрут этапа 4")
                .param("routeType", "BUS")
                .param("startPoint", "Депо")
                .param("endPoint", "Вокзал")
                .param("lengthKm", "18.75")
                .param("active", "true"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/routes"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM route WHERE route_number = ? AND route_type = ?",
                "S4-77",
                "BUS",
            ),
        ).isEqualTo(1)
    }

    @Test
    fun superadminCanCreateUser() {
        val superadminSession = loginAs("superadmin", "superadmin")
        val viewerRoleId = longValue("SELECT id FROM role WHERE name::text = ?", "VIEWER")

        mockMvc.perform(
            post("/users")
                .session(superadminSession)
                .with(csrf())
                .param("username", "Stage4.User")
                .param("rawPassword", "stage4pass")
                .param("active", "true")
                .param("roleIds", viewerRoleId.toString()),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/users"))

        val userId = longValue("SELECT id FROM app_user WHERE username = ?", "stage4.user")
        val passwordHash = stringValue("SELECT password_hash FROM app_user WHERE id = ?", userId)

        assertThat(passwordHash).startsWith("\$2")
        assertThat(passwordHash).isNotEqualTo("stage4pass")
        assertThat(
            longValue("SELECT COUNT(*) FROM user_role WHERE user_id = ? AND role_id = ?", userId, viewerRoleId),
        ).isEqualTo(1)
    }

    @Test
    fun superadminCannotRemoveLastActiveSuperadmin() {
        val superadminSession = loginAs("superadmin", "superadmin")
        val superadminId = longValue("SELECT id FROM app_user WHERE username = ?", "superadmin")
        val adminRoleId = longValue("SELECT id FROM role WHERE name::text = ?", "ADMIN")

        mockMvc.perform(
            post("/users/$superadminId")
                .session(superadminSession)
                .with(csrf())
                .param("username", "superadmin")
                .param("active", "false")
                .param("roleIds", adminRoleId.toString()),
        )
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("В системе должен остаться хотя бы один активный SUPERADMIN.")))

        assertThat(
            longValue(
                """
                SELECT COUNT(*)
                FROM app_user u
                JOIN user_role ur ON ur.user_id = u.id
                JOIN role r ON r.id = ur.role_id
                WHERE u.is_active = TRUE AND r.name::text = 'SUPERADMIN'
                """.trimIndent(),
            ),
        ).isEqualTo(1)
    }

    @Test
    fun viewerCanOpenAllStageFourCrudListsButCannotOpenProtectedForms() {
        val viewerSession = loginAs("viewer", "viewer")

        listOf(
            "/organization",
            "/workshops",
            "/organization-sections",
            "/brigades",
            "/employee-brigade-assignments",
            "/vehicle-driver-assignments",
            "/garage",
            "/garage-objects",
            "/vehicle-locations",
            "/transportation",
            "/route-vehicle-assignments",
            "/transportation-records",
            "/repairs",
            "/repair-types",
            "/repairs-journal",
            "/repair-works",
            "/components",
            "/components-catalog",
            "/component-history",
            "/vehicle-acquisitions",
            "/vehicle-disposals",
        ).forEach { path ->
            mockMvc.perform(get(path).session(viewerSession))
                .andExpect(status().isOk())
        }

        listOf(
            "/workshops/new",
            "/garage-objects/new",
            "/route-vehicle-assignments/new",
            "/repairs-journal/new",
            "/components-catalog/new",
            "/vehicle-acquisitions/new",
        ).forEach { path ->
            mockMvc.perform(get(path).session(viewerSession))
                .andExpect(status().isForbidden())
        }
    }

    @Test
    fun adminCanOpenRepresentativeCreateAndEditForms() {
        val adminSession = loginAs("admin", "admin")

        listOf(
            "/vehicles/new",
            "/vehicles/1/edit",
            "/routes/new",
            "/routes/1/edit",
            "/workshops/new",
            "/workshops/1/edit",
            "/organization-sections/new",
            "/organization-sections/1/edit",
        ).forEach { path ->
            mockMvc.perform(get(path).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.isEmptyOrNullString())))
        }
    }

    @Test
    fun adminCanCreateOrganizationGarageAndVehicleDocumentRecords() {
        val adminSession = loginAs("admin", "admin")

        mockMvc.perform(
            post("/workshops")
                .session(adminSession)
                .with(csrf())
                .param("name", "Stage4 Workshop")
                .param("description", "Workshop created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/workshops"))

        val workshopId = longValue(
            "SELECT id FROM workshop WHERE name = ?",
            "Stage4 Workshop",
        )

        mockMvc.perform(
            post("/organization-sections")
                .session(adminSession)
                .with(csrf())
                .param("workshopId", workshopId.toString())
                .param("name", "Stage4 Section")
                .param("description", "Section created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/organization-sections"))

        val sectionId = longValue(
            "SELECT id FROM section WHERE workshop_id = ? AND name = ?",
            workshopId,
            "Stage4 Section",
        )

        mockMvc.perform(
            post("/brigades")
                .session(adminSession)
                .with(csrf())
                .param("sectionId", sectionId.toString())
                .param("name", "Stage4 Brigade")
                .param("description", "Brigade created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/brigades"))

        val brigadeId = longValue(
            "SELECT id FROM brigade WHERE section_id = ? AND name = ?",
            sectionId,
            "Stage4 Brigade",
        )

        mockMvc.perform(
            post("/garage-objects")
                .session(adminSession)
                .with(csrf())
                .param("name", "Stage4 Garage Object")
                .param("objectType", "BOX")
                .param("workshopId", workshopId.toString())
                .param("sectionId", sectionId.toString())
                .param("capacity", "3")
                .param("address", "г. Город, ул. Тестовая, 4")
                .param("description", "Garage object created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/garage-objects"))

        val garageObjectId = longValue(
            "SELECT id FROM garage_object WHERE name = ?",
            "Stage4 Garage Object",
        )

        jdbcTemplate.update(
            "UPDATE vehicle_location_history SET end_date = ?::date WHERE vehicle_id = ? AND end_date IS NULL",
            "2026-04-26",
            4L,
        )

        mockMvc.perform(
            post("/vehicle-locations")
                .session(adminSession)
                .with(csrf())
                .param("vehicleId", "4")
                .param("garageObjectId", garageObjectId.toString())
                .param("startDate", "2026-04-27")
                .param("notes", "Vehicle location created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicle-locations"))

        mockMvc.perform(
            post("/vehicle-acquisitions")
                .session(adminSession)
                .with(csrf())
                .param("vehicleId", "6")
                .param("acquisitionDate", "2026-04-27")
                .param("acquisitionType", "TRANSFER")
                .param("supplierName", "Stage4 Supplier")
                .param("documentNumber", "S4-ACQ-001")
                .param("cost", "150000.00")
                .param("notes", "Acquisition created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicle-acquisitions"))

        mockMvc.perform(
            post("/vehicle-disposals")
                .session(adminSession)
                .with(csrf())
                .param("vehicleId", "3")
                .param("disposalDate", "2026-04-27")
                .param("disposalType", "TRANSFER")
                .param("reason", "Transfer to affiliated depot")
                .param("documentNumber", "S4-DSP-001")
                .param("amountReceived", "50000.00")
                .param("notes", "Disposal created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicle-disposals"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM brigade WHERE id = ? AND name = ?",
                brigadeId,
                "Stage4 Brigade",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle_location_history WHERE vehicle_id = ? AND garage_object_id = ? AND start_date = ?::date",
                4L,
                garageObjectId,
                "2026-04-27",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle_acquisition WHERE document_number = ?",
                "S4-ACQ-001",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle_disposal WHERE document_number = ?",
                "S4-DSP-001",
            ),
        ).isEqualTo(1)
    }

    @Test
    fun hrCanCreateEmployeeBrigadeAssignment() {
        val hrSession = loginAs("hr", "hr")

        mockMvc.perform(
            post("/employee-brigade-assignments")
                .session(hrSession)
                .with(csrf())
                .param("employeeId", "10")
                .param("brigadeId", "1")
                .param("startDate", "2026-04-27")
                .param("primary", "true"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/employee-brigade-assignments"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM employee_brigade_assignment WHERE employee_id = ? AND brigade_id = ? AND start_date = ?::date",
                10L,
                1L,
                "2026-04-27",
            ),
        ).isEqualTo(1)
    }

    @Test
    fun dispatcherCanCreateAssignmentsAndTransportationRecord() {
        val dispatcherSession = loginAs("dispatcher", "dispatcher")
        val mileageBefore = decimalValue("SELECT current_mileage FROM vehicle WHERE id = ?", 6L)

        mockMvc.perform(
            post("/vehicle-driver-assignments")
                .session(dispatcherSession)
                .with(csrf())
                .param("vehicleId", "6")
                .param("driverEmployeeId", "4")
                .param("startDate", "2026-04-27")
                .param("assignmentType", "SHIFT")
                .param("notes", "Driver assignment created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/vehicle-driver-assignments"))

        mockMvc.perform(
            post("/route-vehicle-assignments")
                .session(dispatcherSession)
                .with(csrf())
                .param("routeId", "1")
                .param("vehicleId", "1")
                .param("startDate", "2026-04-27")
                .param("shiftInfo", "Вечерняя смена")
                .param("notes", "Route assignment created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/route-vehicle-assignments"))

        mockMvc.perform(
            post("/transportation-records")
                .session(dispatcherSession)
                .with(csrf())
                .param("vehicleId", "6")
                .param("recordType", "SERVICE")
                .param("recordDate", "2026-04-27")
                .param("mileageKm", "25.00")
                .param("hoursUsed", "4.50")
                .param("tripCount", "1")
                .param("description", "Service transportation created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/transportation-records"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle_driver_assignment WHERE vehicle_id = ? AND driver_employee_id = ? AND start_date = ?::date",
                6L,
                4L,
                "2026-04-27",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM route_vehicle_assignment WHERE route_id = ? AND vehicle_id = ? AND start_date = ?::date",
                1L,
                1L,
                "2026-04-27",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM transportation_record WHERE vehicle_id = ? AND record_date = ?::date AND record_type = ?",
                6L,
                "2026-04-27",
                "SERVICE",
            ),
        ).isEqualTo(1)
        assertThat(decimalValue("SELECT current_mileage FROM vehicle WHERE id = ?", 6L))
            .isEqualByComparingTo(mileageBefore.add(BigDecimal("25.00")))
    }

    @Test
    fun mechanicCanCreateRepairWorkComponentAndHistory() {
        val mechanicSession = loginAs("mechanic", "mechanic")

        mockMvc.perform(
            post("/components-catalog")
                .session(mechanicSession)
                .with(csrf())
                .param("componentType", "BATTERY")
                .param("serialNumber", "S4-COMP-001")
                .param("model", "Stage4 Model")
                .param("manufacturer", "Stage4 Manufacturer")
                .param("status", "IN_STOCK")
                .param("notes", "Component created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/components-catalog"))

        val componentId = longValue(
            "SELECT id FROM component WHERE serial_number = ?",
            "S4-COMP-001",
        )

        mockMvc.perform(
            post("/repairs-journal")
                .session(mechanicSession)
                .with(csrf())
                .param("vehicleId", "6")
                .param("repairTypeId", "1")
                .param("workshopId", "1")
                .param("sectionId", "1")
                .param("brigadeId", "1")
                .param("startDate", "2026-04-27")
                .param("status", "IN_PROGRESS")
                .param("reason", "Stage 4 completion repair")
                .param("description", "Repair created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/repairs-journal"))

        val repairId = longValue(
            "SELECT id FROM repair WHERE vehicle_id = ? AND reason = ?",
            6L,
            "Stage 4 completion repair",
        )

        mockMvc.perform(
            post("/repair-works")
                .session(mechanicSession)
                .with(csrf())
                .param("repairId", repairId.toString())
                .param("employeeId", "8")
                .param("workType", "Диагностика Stage4")
                .param("description", "Repair work created from stage 4 completion test")
                .param("quantity", "1.00")
                .param("cost", "1234.50")
                .param("completedAt", "2026-04-27T12:30:00")
                .param("notes", "Repair work note"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/repair-works"))

        mockMvc.perform(
            post("/component-history")
                .session(mechanicSession)
                .with(csrf())
                .param("vehicleId", "6")
                .param("componentId", componentId.toString())
                .param("repairId", repairId.toString())
                .param("actionType", "INSTALLED")
                .param("actionDate", "2026-04-27")
                .param("cost", "500.00")
                .param("notes", "Component history created from stage 4 completion test"),
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/component-history"))

        assertThat(
            longValue(
                "SELECT COUNT(*) FROM repair_work WHERE repair_id = ? AND work_type = ?",
                repairId,
                "Диагностика Stage4",
            ),
        ).isEqualTo(1)
        assertThat(
            longValue(
                "SELECT COUNT(*) FROM vehicle_component_history WHERE repair_id = ? AND component_id = ? AND action_type = ?",
                repairId,
                componentId,
                "INSTALLED",
            ),
        ).isEqualTo(1)
        assertThat(decimalValue("SELECT total_cost FROM repair WHERE id = ?", repairId))
            .isEqualByComparingTo(BigDecimal("1734.50"))
        assertThat(stringValue("SELECT status FROM component WHERE id = ?", componentId))
            .isEqualTo("INSTALLED")
    }

    private fun loginAs(username: String, password: String): MockHttpSession =
        mockMvc.perform(formLogin().user(username).password(password))
            .andExpect(status().is3xxRedirection)
            .andExpect(authenticated().withUsername(username))
            .andReturn()
            .request
            .session as MockHttpSession

    private fun longValue(sql: String, vararg args: Any): Long =
        jdbcTemplate.queryForObject(sql, Long::class.java, *args)

    private fun decimalValue(sql: String, vararg args: Any): BigDecimal =
        jdbcTemplate.queryForObject(sql, BigDecimal::class.java, *args)

    private fun stringValue(sql: String, vararg args: Any): String =
        jdbcTemplate.queryForObject(sql, String::class.java, *args)

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
