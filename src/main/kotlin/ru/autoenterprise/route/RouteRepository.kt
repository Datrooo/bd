package ru.autoenterprise.route

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RouteRepository : JpaRepository<RouteEntity, Long> {
    fun findAllBy(pageable: Pageable): Page<RouteEntity>
}
