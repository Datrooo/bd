package ru.autoenterprise.report

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import ru.autoenterprise.component.ComponentService
import ru.autoenterprise.employee.EmployeeService
import ru.autoenterprise.vehicle.VehicleCategoryService
import ru.autoenterprise.vehicle.VehicleService

@Service
class ReportService(
    private val reportCatalog: ReportCatalog,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val resourceLoader: ResourceLoader,
    private val vehicleService: VehicleService,
    private val employeeService: EmployeeService,
    private val vehicleCategoryService: VehicleCategoryService,
    private val componentService: ComponentService,
    private val reportProperties: ReportProperties,
) {

    private val sqlCache = ConcurrentHashMap<String, String>()

    init {
        (namedParameterJdbcTemplate.jdbcOperations as? JdbcTemplate)
            ?.queryTimeout = reportProperties.timeoutSeconds.coerceAtLeast(1)
    }

    fun catalogGroups(): List<ReportCatalogGroup> = reportCatalog.groups()

    fun allReports(): List<ReportDefinition> = reportCatalog.all()

    fun findReport(code: String): ReportDefinition? = reportCatalog.find(code)

    fun referenceData(): ReportReferenceData =
        ReportReferenceData(
            vehicles = vehicleService.vehicleOptions().map { vehicle ->
                ReportSelectOption(vehicle.id.toString(), vehicle.label)
            },
            employees = employeeService.employeeOptions().map { employee ->
                ReportSelectOption(employee.id.toString(), employee.fullName)
            },
            categories = vehicleCategoryService.categoryOptions().map { category ->
                ReportSelectOption(category.name, category.name)
            },
            brands = vehicleService.brandOptions().map { brand ->
                ReportSelectOption(brand, brand)
            },
            componentTypes = componentService.componentTypeOptions().map { componentType ->
                ReportSelectOption(componentType, componentType)
            },
        )

    fun execute(definition: ReportDefinition, request: ReportExecutionRequest): ReportTableResult {
        val maxRows = reportProperties.maxRows.coerceAtLeast(1)
        val sql = wrapWithLimit(loadSql(definition.sqlResource), maxRows)
        val params = buildParams(definition, request)

        val extractor = ResultSetExtractor<ReportTableResult> { resultSet ->
            val metadata = resultSet.metaData
            val columns = (1..metadata.columnCount).map { index -> metadata.getColumnLabel(index) }
            val rows = mutableListOf<List<String>>()
            var truncated = false

            while (resultSet.next()) {
                if (rows.size == maxRows) {
                    truncated = true
                    break
                }

                rows += columns.indices.map { index ->
                    formatValue(resultSet.getObject(index + 1))
                }
            }

            ReportTableResult(columns = columns, rows = rows, truncated = truncated)
        }

        return namedParameterJdbcTemplate.query(sql, params, extractor)
            ?: ReportTableResult(emptyList(), emptyList())
    }

    private fun buildParams(
        definition: ReportDefinition,
        request: ReportExecutionRequest,
    ): MapSqlParameterSource {
        val params = MapSqlParameterSource()

        if (definition.requiresVehicle) {
            params.addValue("vehicle_id", request.vehicleId)
        }
        if (definition.requiresEmployee) {
            params.addValue("employee_id", request.employeeId)
        }
        if (definition.requiresCategory) {
            params.addValue("category_name", request.categoryName)
        }
        if (definition.requiresBrand) {
            params.addValue("brand_name", request.brandName)
        }
        if (definition.requiresComponentType) {
            params.addValue("component_type", request.componentType)
        }
        if (definition.requiresDateRange) {
            params.addValue("start_date", request.startDate)
            params.addValue("end_date", request.endDate)
        }

        return params
    }

    private fun loadSql(resourcePath: String): String =
        sqlCache.computeIfAbsent(resourcePath) { path ->
            resourceLoader.getResource("classpath:$path")
                .inputStream
                .bufferedReader(Charsets.UTF_8)
                .use { reader -> reader.readText() }
                .trim()
                .removeSuffix(";")
        }

    private fun wrapWithLimit(sql: String, maxRows: Int): String =
        "SELECT * FROM ($sql) report_result LIMIT ${maxRows + 1}"

    private fun formatValue(value: Any?): String =
        when (value) {
            null -> "—"
            is BigDecimal -> value.setScale(2, RoundingMode.HALF_UP).toPlainString()
            else -> value.toString()
        }
}
