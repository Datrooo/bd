package ru.autoenterprise.organization

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface WorkshopRepository : JpaRepository<WorkshopEntity, Long> {
    @EntityGraph(attributePaths = ["chiefEmployee"])
    fun findAllBy(pageable: Pageable): Page<WorkshopEntity>

    fun findAllByOrderByNameAsc(): List<WorkshopEntity>

    @EntityGraph(attributePaths = ["chiefEmployee"])
    override fun findById(id: Long): java.util.Optional<WorkshopEntity>
}

interface SectionRepository : JpaRepository<SectionEntity, Long> {
    @EntityGraph(attributePaths = ["workshop", "masterEmployee"])
    fun findAllBy(pageable: Pageable): Page<SectionEntity>

    fun findAllByOrderByNameAsc(): List<SectionEntity>

    @EntityGraph(attributePaths = ["workshop", "masterEmployee"])
    override fun findById(id: Long): java.util.Optional<SectionEntity>
}

interface BrigadeRepository : JpaRepository<BrigadeEntity, Long> {
    @EntityGraph(attributePaths = ["section", "section.workshop", "brigadierEmployee"])
    fun findAllBy(pageable: Pageable): Page<BrigadeEntity>

    fun findAllByOrderByNameAsc(): List<BrigadeEntity>

    @EntityGraph(attributePaths = ["section", "section.workshop", "brigadierEmployee"])
    override fun findById(id: Long): java.util.Optional<BrigadeEntity>
}

interface EmployeeBrigadeAssignmentRepository : JpaRepository<EmployeeBrigadeAssignmentEntity, Long> {
    @EntityGraph(attributePaths = ["employee", "brigade", "brigade.section"])
    fun findAllBy(pageable: Pageable): Page<EmployeeBrigadeAssignmentEntity>

    @EntityGraph(attributePaths = ["employee", "brigade", "brigade.section"])
    override fun findById(id: Long): java.util.Optional<EmployeeBrigadeAssignmentEntity>
}

interface VehicleDriverAssignmentRepository : JpaRepository<VehicleDriverAssignmentEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle", "driverEmployee"])
    fun findAllBy(pageable: Pageable): Page<VehicleDriverAssignmentEntity>

    @EntityGraph(attributePaths = ["vehicle", "driverEmployee"])
    override fun findById(id: Long): java.util.Optional<VehicleDriverAssignmentEntity>
}
