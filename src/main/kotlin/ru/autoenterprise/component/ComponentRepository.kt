package ru.autoenterprise.component

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface ComponentRepository : JpaRepository<ComponentEntity, Long> {
    fun findAllByOrderBySerialNumberAsc(): List<ComponentEntity>

    fun findAllBy(pageable: Pageable): Page<ComponentEntity>
}

interface VehicleComponentHistoryRepository : JpaRepository<VehicleComponentHistoryEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle", "component", "repair"])
    fun findAllBy(pageable: Pageable): Page<VehicleComponentHistoryEntity>

    @EntityGraph(attributePaths = ["vehicle", "component", "repair"])
    override fun findById(id: Long): java.util.Optional<VehicleComponentHistoryEntity>
}
