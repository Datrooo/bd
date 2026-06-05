package ru.autoenterprise.vehicle

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VehicleCategoryRepository : JpaRepository<VehicleCategoryEntity, Long> {
    fun findAllByOrderByNameAsc(): List<VehicleCategoryEntity>
}

interface VehicleRepository : JpaRepository<VehicleEntity, Long> {
    @EntityGraph(attributePaths = ["category"])
    fun findAllBy(pageable: Pageable): Page<VehicleEntity>

    @EntityGraph(attributePaths = ["category"])
    override fun findById(id: Long): java.util.Optional<VehicleEntity>

    @Query(
        """
        select distinct v.brandName
        from VehicleEntity v
        where v.brandName is not null and trim(v.brandName) <> ''
        order by v.brandName asc
        """,
    )
    fun findDistinctBrandNames(): List<String>
}
