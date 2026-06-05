package ru.autoenterprise.vehicle

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.VehicleAcquisitionType
import ru.autoenterprise.domain.VehicleDisposalType

data class VehicleAcquisitionRow(
    val id: Long,
    val vehicleLabel: String,
    val acquisitionDate: LocalDate,
    val acquisitionType: String,
    val supplierName: String?,
    val cost: BigDecimal?,
)

data class VehicleAcquisitionForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Укажите дату поступления.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var acquisitionDate: LocalDate? = null,
    var acquisitionType: VehicleAcquisitionType = VehicleAcquisitionType.PURCHASE,
    @field:Size(max = 150, message = "Поставщик не должен превышать 150 символов.")
    var supplierName: String? = null,
    @field:Size(max = 100, message = "Номер документа не должен превышать 100 символов.")
    var documentNumber: String? = null,
    @field:DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной.")
    var cost: BigDecimal? = null,
    var notes: String? = null,
)

data class VehicleDisposalRow(
    val id: Long,
    val vehicleLabel: String,
    val disposalDate: LocalDate,
    val disposalType: String,
    val amountReceived: BigDecimal?,
)

data class VehicleDisposalForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Укажите дату выбытия.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var disposalDate: LocalDate? = null,
    var disposalType: VehicleDisposalType = VehicleDisposalType.WRITE_OFF,
    var reason: String? = null,
    @field:Size(max = 100, message = "Номер документа не должен превышать 100 символов.")
    var documentNumber: String? = null,
    @field:DecimalMin(value = "0.00", message = "Сумма поступления не может быть отрицательной.")
    var amountReceived: BigDecimal? = null,
    var notes: String? = null,
)
