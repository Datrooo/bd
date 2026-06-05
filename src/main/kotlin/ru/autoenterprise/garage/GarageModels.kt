package ru.autoenterprise.garage

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.GarageObjectType

data class GarageObjectRow(
    val id: Long,
    val name: String,
    val objectType: String,
    val parentName: String?,
    val workshopName: String?,
    val sectionName: String?,
    val capacity: Int?,
)

data class GarageObjectOption(
    val id: Long,
    val name: String,
)

data class GarageObjectForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите название объекта.")
    @field:Size(max = 150, message = "Название объекта не должно превышать 150 символов.")
    var name: String = "",
    var objectType: GarageObjectType = GarageObjectType.GARAGE,
    var parentObjectId: Long? = null,
    var workshopId: Long? = null,
    var sectionId: Long? = null,
    @field:Size(max = 255, message = "Адрес не должен превышать 255 символов.")
    var address: String? = null,
    @field:Min(value = 0, message = "Вместимость не может быть отрицательной.")
    var capacity: Int? = null,
    var description: String? = null,
)

data class VehicleLocationHistoryRow(
    val id: Long,
    val vehicleLabel: String,
    val garageObjectName: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class VehicleLocationHistoryForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Выберите объект гаражного хозяйства.")
    var garageObjectId: Long? = null,
    @field:NotNull(message = "Укажите дату начала.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
    var notes: String? = null,
)
