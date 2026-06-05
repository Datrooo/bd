package ru.autoenterprise

import org.assertj.core.api.Assertions.assertThatThrownBy
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
class GarageIntegrityIntegrationTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun vehicleLocationHistoryRejectsOverlappingPeriodsForSameVehicle() {
        val vehicleId = createVehicle("OVL1")
        val firstGarageObjectId = createGarageObject("Overlap Test Garage A", 10)
        val secondGarageObjectId = createGarageObject("Overlap Test Garage B", 10)

        insertLocation(vehicleId, firstGarageObjectId, "2030-01-01", "2030-01-10")

        assertThatThrownBy {
            insertLocation(vehicleId, secondGarageObjectId, "2030-01-05", "2030-01-20")
        }.hasMessageContaining("Период размещения транспорта")
    }

    @Test
    fun vehicleLocationHistoryRejectsGarageCapacityOverflow() {
        val garageObjectId = createGarageObject("Capacity Test Garage", 1)
        val firstVehicleId = createVehicle("CAP1")
        val secondVehicleId = createVehicle("CAP2")

        insertLocation(firstVehicleId, garageObjectId, "2031-01-01", null)

        assertThatThrownBy {
            insertLocation(secondVehicleId, garageObjectId, "2031-01-02", null)
        }.hasMessageContaining("Вместимость объекта гаражного хозяйства")
    }

    @Test
    fun garageObjectRejectsCapacityReductionBelowExistingOccupancy() {
        val garageObjectId = createGarageObject("Capacity Update Test Garage", 2)
        val firstVehicleId = createVehicle("CPU1")
        val secondVehicleId = createVehicle("CPU2")

        insertLocation(firstVehicleId, garageObjectId, "2032-01-01", "2032-01-10")
        insertLocation(secondVehicleId, garageObjectId, "2032-01-02", "2032-01-09")

        assertThatThrownBy {
            jdbcTemplate.update("UPDATE garage_object SET capacity = 1 WHERE id = ?", garageObjectId)
        }.hasMessageContaining("Вместимость объекта гаражного хозяйства")
    }

    @Test
    fun garageObjectRejectsParentCycles() {
        val parentId = createGarageObject("Cycle Test Parent", 10)
        val childId = jdbcTemplate.queryForObject(
            """
            INSERT INTO garage_object (name, object_type, parent_object_id, capacity)
            VALUES (?, 'BOX', ?, 10)
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            "Cycle Test Child",
            parentId,
        )!!

        assertThatThrownBy {
            jdbcTemplate.update("UPDATE garage_object SET parent_object_id = ? WHERE id = ?", childId, parentId)
        }.hasMessageContaining("потомка")
    }

    private fun createGarageObject(name: String, capacity: Int): Long =
        jdbcTemplate.queryForObject(
            """
            INSERT INTO garage_object (name, object_type, capacity)
            VALUES (?, 'GARAGE', ?)
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            name,
            capacity,
        )!!

    private fun createVehicle(suffix: String): Long =
        jdbcTemplate.queryForObject(
            """
            INSERT INTO vehicle (inventory_number, registration_number, category_id, brand_name, model_name, status)
            VALUES (?, ?, 1, 'TestBrand', 'TestModel', 'ACTIVE')
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            "TEST-$suffix",
            "T-$suffix",
        )!!

    private fun insertLocation(
        vehicleId: Long,
        garageObjectId: Long,
        startDate: String,
        endDate: String?,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO vehicle_location_history (vehicle_id, garage_object_id, start_date, end_date)
            VALUES (?, ?, ?::date, ?::date)
            """.trimIndent(),
            vehicleId,
            garageObjectId,
            startDate,
            endDate,
        )
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
