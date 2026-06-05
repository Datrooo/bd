package ru.autoenterprise.organization

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.VehicleDriverAssignmentType

data class WorkshopRow(
    val id: Long,
    val name: String,
    val chiefEmployeeName: String?,
)

data class WorkshopOption(
    val id: Long,
    val name: String,
)

data class WorkshopForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите название цеха.")
    @field:Size(max = 150, message = "Название цеха не должно превышать 150 символов.")
    var name: String = "",
    var chiefEmployeeId: Long? = null,
    var description: String? = null,
)

data class SectionRow(
    val id: Long,
    val name: String,
    val workshopName: String,
    val masterEmployeeName: String?,
)

data class SectionOption(
    val id: Long,
    val label: String,
)

data class SectionForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите цех.")
    var workshopId: Long? = null,
    @field:NotBlank(message = "Укажите название участка.")
    @field:Size(max = 150, message = "Название участка не должно превышать 150 символов.")
    var name: String = "",
    var masterEmployeeId: Long? = null,
    var description: String? = null,
)

data class BrigadeRow(
    val id: Long,
    val name: String,
    val sectionName: String,
    val workshopName: String,
    val brigadierEmployeeName: String?,
)

data class BrigadeOption(
    val id: Long,
    val label: String,
)

data class BrigadeForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите участок.")
    var sectionId: Long? = null,
    @field:NotBlank(message = "Укажите название бригады.")
    @field:Size(max = 150, message = "Название бригады не должно превышать 150 символов.")
    var name: String = "",
    var brigadierEmployeeId: Long? = null,
    var description: String? = null,
)

data class EmployeeBrigadeAssignmentRow(
    val id: Long,
    val employeeName: String,
    val brigadeName: String,
    val sectionName: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val primary: Boolean,
)

data class EmployeeBrigadeAssignmentForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите сотрудника.")
    var employeeId: Long? = null,
    @field:NotNull(message = "Выберите бригаду.")
    var brigadeId: Long? = null,
    @field:NotNull(message = "Укажите дату начала.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
    var primary: Boolean = true,
)

data class VehicleDriverAssignmentRow(
    val id: Long,
    val vehicleLabel: String,
    val driverName: String,
    val assignmentType: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class VehicleDriverAssignmentForm(
    val id: Long? = null,
    @field:NotNull(message = "Выберите транспорт.")
    var vehicleId: Long? = null,
    @field:NotNull(message = "Выберите водителя.")
    var driverEmployeeId: Long? = null,
    @field:NotNull(message = "Укажите дату начала.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
    var assignmentType: VehicleDriverAssignmentType = VehicleDriverAssignmentType.PRIMARY,
    var notes: String? = null,
)
