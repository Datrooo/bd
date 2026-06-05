package ru.autoenterprise.repair

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.RepairStatus

data class RepairTypeRow(
    val id: Long,
    val name: String,
    val description: String?,
)

data class RepairTypeOption(
    val id: Long,
    val name: String,
)

data class RepairTypeForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите название типа ремонта.")
    @field:Size(max = 100, message = "Название типа ремонта не должно превышать 100 символов.")
    var name: String = "",
    var description: String? = null,
)

data class RepairRow(
    val id: Long,
    val vehicleLabel: String,
    val repairTypeName: String,
    val status: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val totalCost: BigDecimal,
)

data class RepairOption(
    val id: Long,
    val label: String,
)

data class RepairForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Выберите тип ремонта.")
    var repairTypeId: Long? = null,
    var workshopId: Long? = null,
    var sectionId: Long? = null,
    var brigadeId: Long? = null,
    @field:NotNull(message = "Укажите дату начала.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
    var reason: String? = null,
    var description: String? = null,
    var status: RepairStatus = RepairStatus.PLANNED,
    var notes: String? = null,
)

data class RepairWorkRow(
    val id: Long,
    val repairLabel: String,
    val employeeName: String?,
    val workType: String,
    val quantity: BigDecimal,
    val cost: BigDecimal,
    val completedAt: LocalDateTime?,
)

data class RepairWorkForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите ремонт.")
    var repairId: Long? = null,
    var employeeId: Long? = null,
    @field:NotBlank(message = "Укажите тип работы.")
    @field:Size(max = 150, message = "Тип работы не должен превышать 150 символов.")
    var workType: String = "",
    var description: String? = null,
    @field:NotNull(message = "Укажите количество.")
    @field:DecimalMin(value = "0.01", message = "Количество должно быть больше нуля.")
    var quantity: BigDecimal? = BigDecimal.ONE,
    @field:NotNull(message = "Укажите стоимость.")
    @field:DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной.")
    var cost: BigDecimal? = BigDecimal.ZERO,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var completedAt: LocalDateTime? = null,
    var notes: String? = null,
)
