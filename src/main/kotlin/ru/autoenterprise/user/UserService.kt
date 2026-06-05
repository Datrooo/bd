package ru.autoenterprise.user

import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.employee.EmployeeRepository
import jakarta.persistence.EntityNotFoundException
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.security.AppRole

@Service
class UserService(
    private val userRepository: AppUserRepository,
    private val roleRepository: RoleRepository,
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun listUsers(page: Int): Page<UserRow> =
        userRepository.findAllBy(pageable(page)).map { user ->
            UserRow(
                id = user.id!!,
                username = user.username,
                employeeName = user.employee?.let(::employeeName),
                rolesLabel = user.roles.map { role -> role.code.roleName }.sorted().joinToString(", "),
                active = user.active,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt,
            )
        }

    fun roleOptions(): List<RoleOption> =
        roleRepository.findAllByOrderByCodeAsc().map { role -> RoleOption(role.id!!, role.code.roleName) }

    fun getUserForm(id: Long): UserForm {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Пользователь не найден.") }

        return UserForm(
            id = user.id,
            username = user.username,
            rawPassword = "",
            employeeId = user.employee?.id,
            active = user.active,
            roleIds = user.roles.mapNotNull { it.id }.sorted().toMutableList(),
        )
    }

    @Transactional
    fun createUser(form: UserForm) {
        val user = AppUserEntity(
            createdAt = LocalDateTime.now(),
            lastLoginAt = null,
        )
        applyUser(user, form, true)
        userRepository.save(user)
    }

    @Transactional
    fun updateUser(id: Long, form: UserForm) {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Пользователь не найден.") }

        applyUser(user, form, false)
    }

    @Transactional
    fun deleteUser(id: Long, currentUsername: String) {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Пользователь не найден.") }

        if (user.username.equals(currentUsername, ignoreCase = true)) {
            throw IllegalStateException("Нельзя удалить текущую учетную запись.")
        }

        ensureActiveSuperadminPreserved(
            excludedUserId = user.id,
            resultingActive = false,
            resultingRoles = emptyList(),
        )

        userRepository.delete(user)
    }

    companion object {
        private const val pageSize = 10

        private fun pageable(page: Int): PageRequest =
            PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by("username").ascending())

        private fun employeeName(employee: EmployeeEntity): String =
            listOfNotNull(employee.lastName, employee.firstName, employee.middleName).joinToString(" ")
    }

    private fun applyUser(user: AppUserEntity, form: UserForm, creating: Boolean) {
        if (creating || form.rawPassword.isNotBlank()) {
            user.passwordHash = passwordEncoder.encode(form.rawPassword.trim())
        }

        val roles = roleRepository.findAllById(form.roleIds.toSet())
        if (roles.size != form.roleIds.toSet().size) {
            throw EntityNotFoundException("Не все выбранные роли существуют.")
        }

        if (!creating) {
            ensureActiveSuperadminPreserved(
                excludedUserId = user.id,
                resultingActive = form.active,
                resultingRoles = roles,
            )
        }

        user.username = normalizeUsername(form.username)
        user.employee = form.employeeId?.let { employeeId ->
            employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException("Связанный сотрудник не найден.") }
        }
        user.active = form.active

        user.roles.clear()
        user.roles.addAll(roles)
    }

    private fun ensureActiveSuperadminPreserved(
        excludedUserId: Long?,
        resultingActive: Boolean,
        resultingRoles: Collection<RoleEntity>,
    ) {
        val otherActiveSuperadmins = userRepository.countActiveUsersByRoleExcludingUserId(
            role = AppRole.SUPERADMIN.name,
            excludedUserId = excludedUserId,
        )
        val currentUserRemainsActiveSuperadmin = resultingActive && resultingRoles.any { role -> role.code == AppRole.SUPERADMIN }
        val resultingActiveSuperadminCount = otherActiveSuperadmins + if (currentUserRemainsActiveSuperadmin) 1 else 0

        if (resultingActiveSuperadminCount == 0L) {
            throw IllegalStateException("В системе должен остаться хотя бы один активный SUPERADMIN.")
        }
    }

    private fun normalizeUsername(rawUsername: String): String =
        rawUsername.trim().lowercase()
}
