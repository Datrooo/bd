package ru.autoenterprise.repair

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RepairTypeRepository : JpaRepository<RepairTypeEntity, Long> {
    fun findAllByOrderByNameAsc(): List<RepairTypeEntity>
}

interface RepairRepository : JpaRepository<RepairEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle", "repairType", "workshop", "section", "brigade"])
    fun findAllBy(pageable: Pageable): Page<RepairEntity>

    @EntityGraph(attributePaths = ["vehicle", "repairType", "workshop", "section", "brigade"])
    override fun findById(id: Long): java.util.Optional<RepairEntity>
}

interface RepairWorkRepository : JpaRepository<RepairWorkEntity, Long> {
    @EntityGraph(attributePaths = ["repair", "repair.vehicle", "employee"])
    fun findAllBy(pageable: Pageable): Page<RepairWorkEntity>

    @EntityGraph(attributePaths = ["repair", "repair.vehicle", "employee"])
    override fun findById(id: Long): java.util.Optional<RepairWorkEntity>
}
