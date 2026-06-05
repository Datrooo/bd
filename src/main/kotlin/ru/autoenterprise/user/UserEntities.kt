package ru.autoenterprise.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.security.AppRole

@Entity
@Table(name = "role")
class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, columnDefinition = "app_role")
    var code: AppRole = AppRole.VIEWER,
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "app_user")
class AppUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 100)
    var username: String = "",
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: EmployeeEntity? = null,
    @Column(name = "is_active", nullable = false)
    var active: Boolean = true,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: MutableSet<RoleEntity> = linkedSetOf(),
)
