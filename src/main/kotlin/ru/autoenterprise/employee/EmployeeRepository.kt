package ru.autoenterprise.employee

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<EmployeeEntity, Long> {
    fun findAllBy(pageable: Pageable): Page<EmployeeEntity>
}
