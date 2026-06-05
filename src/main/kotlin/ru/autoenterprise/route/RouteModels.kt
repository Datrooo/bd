package ru.autoenterprise.route

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import ru.autoenterprise.domain.RouteType

data class RouteRow(
    val id: Long,
    val routeNumber: String,
    val name: String,
    val routeType: String,
    val startPoint: String,
    val endPoint: String,
    val lengthKm: BigDecimal?,
    val active: Boolean,
)

data class RouteReferenceOption(
    val id: Long,
    val label: String,
)

data class RouteForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите номер маршрута.")
    @field:Size(max = 20, message = "Номер маршрута не должен превышать 20 символов.")
    var routeNumber: String = "",
    @field:NotBlank(message = "Укажите название маршрута.")
    @field:Size(max = 150, message = "Название маршрута не должно превышать 150 символов.")
    var name: String = "",
    var routeType: RouteType = RouteType.BUS,
    @field:NotBlank(message = "Укажите начальную точку.")
    @field:Size(max = 150, message = "Начальная точка не должна превышать 150 символов.")
    var startPoint: String = "",
    @field:NotBlank(message = "Укажите конечную точку.")
    @field:Size(max = 150, message = "Конечная точка не должна превышать 150 символов.")
    var endPoint: String = "",
    @field:DecimalMin(value = "0.00", message = "Протяженность не может быть отрицательной.")
    var lengthKm: BigDecimal? = null,
    var active: Boolean = true,
    var notes: String? = null,
)
