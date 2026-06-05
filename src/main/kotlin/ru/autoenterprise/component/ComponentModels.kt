package ru.autoenterprise.component

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.ComponentStatus
import ru.autoenterprise.domain.VehicleComponentActionType

data class ComponentRow(
    val id: Long,
    val componentType: String,
    val serialNumber: String,
    val model: String?,
    val manufacturer: String?,
    val status: String,
)

data class ComponentOption(
    val id: Long,
    val label: String,
)

data class ComponentForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите тип агрегата.")
    @field:Size(max = 100, message = "Тип агрегата не должен превышать 100 символов.")
    var componentType: String = "",
    @field:NotBlank(message = "Укажите серийный номер.")
    @field:Size(max = 100, message = "Серийный номер не должен превышать 100 символов.")
    var serialNumber: String = "",
    @field:Size(max = 100, message = "Модель не должна превышать 100 символов.")
    var model: String? = null,
    @field:Size(max = 100, message = "Производитель не должен превышать 100 символов.")
    var manufacturer: String? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var productionDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var purchaseDate: LocalDate? = null,
    var status: ComponentStatus = ComponentStatus.IN_STOCK,
    var notes: String? = null,
)

data class VehicleComponentHistoryRow(
    val id: Long,
    val vehicleLabel: String,
    val componentLabel: String,
    val repairLabel: String?,
    val actionType: String,
    val actionDate: LocalDate,
    val cost: BigDecimal,
)

data class VehicleComponentHistoryForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Выберите агрегат.")
    var componentId: Long? = null,
    var repairId: Long? = null,
    var actionType: VehicleComponentActionType = VehicleComponentActionType.INSTALLED,
    @field:NotNull(message = "Укажите дату действия.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var actionDate: LocalDate? = null,
    @field:NotNull(message = "Укажите стоимость.")
    @field:DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной.")
    var cost: BigDecimal? = BigDecimal.ZERO,
    var notes: String? = null,
)
