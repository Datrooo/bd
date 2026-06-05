package ru.autoenterprise.vehicle

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.VehicleStatus

data class VehicleCategoryRow(
    val id: Long,
    val name: String,
    val description: String?,
)

data class VehicleCategoryForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите название категории.")
    @field:Size(max = 100, message = "Название категории не должно превышать 100 символов.")
    var name: String = "",
    var description: String? = null,
)

data class VehicleRow(
    val id: Long,
    val inventoryNumber: String,
    val registrationNumber: String,
    val categoryName: String,
    val brandName: String,
    val modelName: String,
    val status: String,
    val currentMileage: BigDecimal,
)

data class VehicleCategoryOption(
    val id: Long,
    val name: String,
)

data class VehicleReferenceOption(
    val id: Long,
    val label: String,
)

data class VehicleForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите инвентарный номер.")
    @field:Size(max = 50, message = "Инвентарный номер не должен превышать 50 символов.")
    var inventoryNumber: String = "",
    @field:NotBlank(message = "Укажите регистрационный номер.")
    @field:Size(max = 20, message = "Регистрационный номер не должен превышать 20 символов.")
    var registrationNumber: String = "",
    @field:Size(max = 50, message = "VIN не должен превышать 50 символов.")
    var vin: String? = null,
    @field:NotNull(message = "Выберите категорию транспорта.")
    var categoryId: Long? = null,
    @field:NotBlank(message = "Укажите марку.")
    @field:Size(max = 100, message = "Марка не должна превышать 100 символов.")
    var brandName: String = "",
    @field:NotBlank(message = "Укажите модель.")
    @field:Size(max = 100, message = "Модель не должна превышать 100 символов.")
    var modelName: String = "",
    @field:Min(value = 1900, message = "Год выпуска должен быть не меньше 1900.")
    @field:Max(value = 2100, message = "Год выпуска выглядит некорректно.")
    var manufactureYear: Int? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var purchaseDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var commissioningDate: LocalDate? = null,
    @field:NotNull(message = "Укажите текущий пробег.")
    @field:DecimalMin(value = "0.00", message = "Пробег не может быть отрицательным.")
    var currentMileage: BigDecimal? = BigDecimal.ZERO,
    var status: VehicleStatus = VehicleStatus.ACTIVE,
    @field:Min(value = 0, message = "Вместимость не может быть отрицательной.")
    var passengerCapacity: Int? = null,
    @field:DecimalMin(value = "0.00", message = "Грузоподъемность не может быть отрицательной.")
    var loadCapacityKg: BigDecimal? = null,
    @field:DecimalMin(value = "0.00", message = "Объем кузова не может быть отрицательным.")
    var cargoVolumeM3: BigDecimal? = null,
    @field:Size(max = 100, message = "Тип кузова не должен превышать 100 символов.")
    var bodyType: String? = null,
    @field:Size(max = 100, message = "Номер лицензии такси не должен превышать 100 символов.")
    var taxiLicenseNumber: String? = null,
    @field:Size(max = 200, message = "Назначение не должно превышать 200 символов.")
    var servicePurpose: String? = null,
    @field:Size(max = 50, message = "Цвет не должен превышать 50 символов.")
    var color: String? = null,
    @field:Size(max = 50, message = "Номер двигателя не должен превышать 50 символов.")
    var engineNumber: String? = null,
    @field:Size(max = 50, message = "Номер шасси не должен превышать 50 символов.")
    var chassisNumber: String? = null,
    var notes: String? = null,
)
