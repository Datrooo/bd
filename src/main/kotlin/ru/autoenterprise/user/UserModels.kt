package ru.autoenterprise.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserRow(
    val id: Long,
    val username: String,
    val employeeName: String?,
    val rolesLabel: String,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?,
)

data class RoleOption(
    val id: Long,
    val name: String,
)

data class UserForm(
    val id: Long? = null,
    @field:NotBlank(message = "Укажите логин.")
    @field:Size(max = 100, message = "Логин не должен превышать 100 символов.")
    var username: String = "",
    var rawPassword: String = "",
    var employeeId: Long? = null,
    var active: Boolean = true,
    var roleIds: MutableList<Long> = mutableListOf(),
)
