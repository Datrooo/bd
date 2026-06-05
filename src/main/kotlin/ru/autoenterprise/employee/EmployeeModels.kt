package ru.autoenterprise.employee

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import ru.autoenterprise.domain.EmployeeStatus

data class EmployeeRow(
    val id: Long,
    val personnelNumber: String,
    val fullName: String,
    val position: String,
    val status: String,
    val hireDate: LocalDate,
)

data class EmployeeOption(
    val id: Long,
    val fullName: String,
)

data class EmployeeForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите табельный номер.")
    @field:Size(max = 50, message = "Табельный номер не должен превышать 50 символов.")
    var personnelNumber: String = "",
    @field:NotBlank(message = "Укажите фамилию.")
    @field:Size(max = 100, message = "Фамилия не должна превышать 100 символов.")
    var lastName: String = "",
    @field:NotBlank(message = "Укажите имя.")
    @field:Size(max = 100, message = "Имя не должно превышать 100 символов.")
    var firstName: String = "",
    @field:Size(max = 100, message = "Отчество не должно превышать 100 символов.")
    var middleName: String? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var birthDate: LocalDate? = null,
    @field:NotNull(message = "Укажите дату приема.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var hireDate: LocalDate? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var dismissalDate: LocalDate? = null,
    @field:NotBlank(message = "Укажите должность.")
    @field:Size(max = 100, message = "Должность не должна превышать 100 символов.")
    var position: String = "",
    @field:Size(max = 200, message = "Квалификация не должна превышать 200 символов.")
    var qualification: String? = null,
    @field:Size(max = 30, message = "Телефон не должен превышать 30 символов.")
    var phone: String? = null,
    @field:Size(max = 150, message = "Email не должен превышать 150 символов.")
    var email: String? = null,
    var address: String? = null,
    var status: EmployeeStatus = EmployeeStatus.ACTIVE,
    var notes: String? = null,
)
