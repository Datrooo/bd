package ru.autoenterprise.garage

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface GarageObjectRepository : JpaRepository<GarageObjectEntity, Long> {
    @EntityGraph(attributePaths = ["parentObject", "workshop", "section"])
    fun findAllBy(pageable: Pageable): Page<GarageObjectEntity>

    @EntityGraph(attributePaths = ["parentObject", "workshop", "section"])
    override fun findById(id: Long): java.util.Optional<GarageObjectEntity>

    fun findAllByOrderByNameAsc(): List<GarageObjectEntity>

    fun existsByParentObjectId(parentObjectId: Long): Boolean
}

interface VehicleLocationHistoryRepository : JpaRepository<VehicleLocationHistoryEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle", "garageObject", "garageObject.parentObject"])
    fun findAllBy(pageable: Pageable): Page<VehicleLocationHistoryEntity>

    @EntityGraph(attributePaths = ["vehicle", "garageObject", "garageObject.parentObject"])
    override fun findById(id: Long): java.util.Optional<VehicleLocationHistoryEntity>

    fun existsByGarageObjectId(garageObjectId: Long): Boolean
}
