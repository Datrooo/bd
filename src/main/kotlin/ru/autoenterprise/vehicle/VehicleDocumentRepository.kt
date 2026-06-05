package ru.autoenterprise.vehicle

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface VehicleAcquisitionRepository : JpaRepository<VehicleAcquisitionEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle"])
    fun findAllBy(pageable: Pageable): Page<VehicleAcquisitionEntity>

    @EntityGraph(attributePaths = ["vehicle"])
    override fun findById(id: Long): java.util.Optional<VehicleAcquisitionEntity>
}

interface VehicleDisposalRepository : JpaRepository<VehicleDisposalEntity, Long> {
    @EntityGraph(attributePaths = ["vehicle"])
    fun findAllBy(pageable: Pageable): Page<VehicleDisposalEntity>

    @EntityGraph(attributePaths = ["vehicle"])
    override fun findById(id: Long): java.util.Optional<VehicleDisposalEntity>
}
