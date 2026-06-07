package ru.autoenterprise.report

import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat

enum class ReportParameterType {
    NONE,
    DATE_RANGE,
    VEHICLE,
    EMPLOYEE,
    CATEGORY,
    BRAND,
    COMPONENT_TYPE,
}

data class ReportParameterDefinition(
    val type: ReportParameterType,
    val label: String,
    val parameterNames: List<String>,
) {
    companion object {
        fun none() = ReportParameterDefinition(ReportParameterType.NONE, "Без параметров", emptyList())

        fun dateRange() = ReportParameterDefinition(
            type = ReportParameterType.DATE_RANGE,
            label = "Период",
            parameterNames = listOf("start_date", "end_date"),
        )

        fun vehicle() = ReportParameterDefinition(
            type = ReportParameterType.VEHICLE,
            label = "Транспорт",
            parameterNames = listOf("vehicle_id"),
        )

        fun employee() = ReportParameterDefinition(
            type = ReportParameterType.EMPLOYEE,
            label = "Сотрудник",
            parameterNames = listOf("employee_id"),
        )

        fun category() = ReportParameterDefinition(
            type = ReportParameterType.CATEGORY,
            label = "Категория",
            parameterNames = listOf("category_name"),
        )

        fun brand() = ReportParameterDefinition(
            type = ReportParameterType.BRAND,
            label = "Марка",
            parameterNames = listOf("brand_name"),
        )

        fun componentType() = ReportParameterDefinition(
            type = ReportParameterType.COMPONENT_TYPE,
            label = "Тип агрегата",
            parameterNames = listOf("component_type"),
        )
    }
}

data class ReportDefinition(
    val code: String,
    val title: String,
    val description: String,
    val groupTitle: String,
    val sqlResource: String,
    val parameters: List<ReportParameterDefinition>,
    val accessRule: String = "isAuthenticated()",
) {
    val activeParameters: List<ReportParameterDefinition>
        get() = parameters.filterNot { parameter -> parameter.type == ReportParameterType.NONE }

    val requiresParameters: Boolean
        get() = activeParameters.isNotEmpty()

    val requiresDateRange: Boolean
        get() = hasParameter(ReportParameterType.DATE_RANGE)

    val requiresVehicle: Boolean
        get() = hasParameter(ReportParameterType.VEHICLE)

    val requiresEmployee: Boolean
        get() = hasParameter(ReportParameterType.EMPLOYEE)

    val requiresCategory: Boolean
        get() = hasParameter(ReportParameterType.CATEGORY)

    val requiresBrand: Boolean
        get() = hasParameter(ReportParameterType.BRAND)

    val requiresComponentType: Boolean
        get() = hasParameter(ReportParameterType.COMPONENT_TYPE)

    val parameterSummary: String
        get() = if (!requiresParameters) {
            "Без параметров"
        } else {
            activeParameters.joinToString(" + ") { parameter -> parameter.label }
        }

    fun hasParameter(type: ReportParameterType): Boolean =
        activeParameters.any { parameter -> parameter.type == type }
}

data class ReportCatalogGroup(
    val title: String,
    val reports: List<ReportDefinition>,
)

data class ReportExecutionRequest(
    val code: String,
    val vehicleId: Long? = null,
    val employeeId: Long? = null,
    val categoryName: String? = null,
    val brandName: String? = null,
    val componentType: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class ReportTableResult(
    val columns: List<String>,
    val rows: List<List<String>>,
    val truncated: Boolean = false,
) {
    val rowCount: Int
        get() = rows.size

    val isEmpty: Boolean
        get() = rows.isEmpty()
}

data class ReportSelectOption(
    val value: String,
    val label: String,
)

data class ReportReferenceData(
    val vehicles: List<ReportSelectOption>,
    val employees: List<ReportSelectOption>,
    val categories: List<ReportSelectOption>,
    val brands: List<ReportSelectOption>,
    val componentTypes: List<ReportSelectOption>,
)

data class ReportRequestForm(
    var vehicleId: Long? = null,
    var employeeId: Long? = null,
    var categoryName: String? = null,
    var brandName: String? = null,
    var componentType: String? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
) {
    fun toExecutionRequest(code: String): ReportExecutionRequest =
        ReportExecutionRequest(
            code = code,
            vehicleId = vehicleId,
            employeeId = employeeId,
            categoryName = categoryName?.trim().takeUnless(String?::isNullOrBlank),
            brandName = brandName?.trim().takeUnless(String?::isNullOrBlank),
            componentType = componentType?.trim().takeUnless(String?::isNullOrBlank),
            startDate = startDate,
            endDate = endDate,
        )
}
