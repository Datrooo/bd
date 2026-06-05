package ru.autoenterprise.transportation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RouteVehicleAssignmentRepository : JpaRepository<RouteVehicleAssignmentEntity, Long> {
    @EntityGraph(attributePaths = ["route", "vehicle"])
    fun findAllBy(pageable: Pageable): Page<RouteVehicleAssignmentEntity>

    @EntityGraph(attributePaths = ["route", "vehicle"])
    override fun findById(id: Long): java.util.Optional<RouteVehicleAssignmentEntity>
}

interface TransportationRecordRepository : JpaRepository<TransportationRecordEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle", "route"])
    fun findAllBy(pageable: Pageable): Page<TransportationRecordEntity>

    @EntityGraph(attributePaths = ["vehicle", "route"])
    override fun findById(id: Long): java.util.Optional<TransportationRecordEntity>
}
