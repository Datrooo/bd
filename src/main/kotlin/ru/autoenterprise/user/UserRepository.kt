package ru.autoenterprise.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.autoenterprise.security.AppRole

interface RoleRepository : JpaRepository<RoleEntity, Long> {
    fun findAllByOrderByCodeAsc(): List<RoleEntity>
}

interface AppUserRepository : JpaRepository<AppUserEntity, Long> {
    @EntityGraph(attributePaths = ["employee", "roles"])
    fun findAllBy(pageable: Pageable): Page<AppUserEntity>

    @EntityGraph(attributePaths = ["employee", "roles"])
    override fun findById(id: Long): java.util.Optional<AppUserEntity>

    @Query(
        value = """
            SELECT COUNT(DISTINCT u.id)
            FROM app_user u
            JOIN user_role ur ON ur.user_id = u.id
            JOIN role r ON r.id = ur.role_id
            WHERE u.is_active = TRUE
              AND r.name::text = :role
              AND (:excludedUserId IS NULL OR u.id <> :excludedUserId)
        """,
        nativeQuery = true,
    )
    fun countActiveUsersByRoleExcludingUserId(
        @Param("role") role: String,
        @Param("excludedUserId") excludedUserId: Long?,
    ): Long
}
