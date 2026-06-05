package ru.autoenterprise.transportation

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.TransportationRecordType

data class RouteVehicleAssignmentRow(
    val id: Long,
    val routeLabel: String,
    val vehicleLabel: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val shiftInfo: String?,
)

data class RouteVehicleAssignmentForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите маршрут.")
    var routeId: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Укажите дату начала.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
    @field:Size(max = 100, message = "Информация о смене не должна превышать 100 символов.")
    var shiftInfo: String? = null,
    var notes: String? = null,
)

data class TransportationRecordRow(
    val id: Long,
    val vehicleLabel: String,
    val routeLabel: String?,
    val recordType: String,
    val recordDate: LocalDate,
    val mileageKm: BigDecimal,
    val tripCount: Int,
    val revenue: BigDecimal?,
)

data class TransportationRecordForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    var routeId: Long? = null,
    var recordType: TransportationRecordType = TransportationRecordType.PASSENGER,
    @field:NotNull(message = "Укажите дату записи.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var recordDate: LocalDate? = null,
    @field:NotNull(message = "Укажите пробег.")
    @field:DecimalMin(value = "0.00", message = "Пробег не может быть отрицательным.")
    var mileageKm: BigDecimal? = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.00", message = "Часы работы не могут быть отрицательными.")
    var hoursUsed: BigDecimal? = null,
    @field:Min(value = 0, message = "Количество пассажиров не может быть отрицательным.")
    var passengerCount: Int? = null,
    @field:DecimalMin(value = "0.00", message = "Вес груза не может быть отрицательным.")
    var cargoWeightKg: BigDecimal? = null,
    @field:DecimalMin(value = "0.00", message = "Объем груза не может быть отрицательным.")
    var cargoVolumeM3: BigDecimal? = null,
    @field:NotNull(message = "Укажите количество рейсов.")
    @field:Min(value = 1, message = "Количество рейсов должно быть больше нуля.")
    var tripCount: Int? = 1,
    @field:DecimalMin(value = "0.00", message = "Выручка не может быть отрицательной.")
    var revenue: BigDecimal? = null,
    var description: String? = null,
    var notes: String? = null,
)
