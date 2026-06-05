package ru.autoenterprise.route

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.RouteType

@Service
class RouteService(
    private val routeRepository: RouteRepository,
) {

    fun listRoutes(page: Int): Page<RouteRow> =
        routeRepository.findAllBy(pageable(page)).map { route ->
            RouteRow(
                id = route.id!!,
                routeNumber = route.routeNumber,
                name = route.name,
                routeType = route.routeType.name,
                startPoint = route.startPoint,
                endPoint = route.endPoint,
                lengthKm = route.lengthKm,
                active = route.active,
            )
        }

    fun getRouteForm(id: Long): RouteForm {
        val route = routeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Маршрут не найден.") }

        return RouteForm(
            id = route.id,
            routeNumber = route.routeNumber,
            name = route.name,
            routeType = route.routeType,
            startPoint = route.startPoint,
            endPoint = route.endPoint,
            lengthKm = route.lengthKm,
            active = route.active,
            notes = route.notes,
        )
    }

    fun routeOptions(): List<RouteReferenceOption> =
        routeRepository.findAll(Sort.by("routeNumber").ascending()).map { route ->
            RouteReferenceOption(
                id = route.id!!,
                label = "${route.routeNumber} / ${route.name}",
            )
        }

    @Transactional
    fun createRoute(form: RouteForm) {
        val route = RouteEntity()
        applyRoute(route, form)
        routeRepository.save(route)
    }

    @Transactional
    fun updateRoute(id: Long, form: RouteForm) {
        val route = routeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Маршрут не найден.") }

        applyRoute(route, form)
    }

    @Transactional
    fun deleteRoute(id: Long) {
        routeRepository.deleteById(id)
    }

    companion object {
        val routeTypes: List<RouteType> = RouteType.entries

        private const val pageSize = 10

        private fun pageable(page: Int): PageRequest =
            PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by("routeNumber").ascending())
    }

    private fun applyRoute(route: RouteEntity, form: RouteForm) {
        route.routeNumber = form.routeNumber.trim()
        route.name = form.name.trim()
        route.routeType = form.routeType
        route.startPoint = form.startPoint.trim()
        route.endPoint = form.endPoint.trim()
        route.lengthKm = form.lengthKm
        route.active = form.active
        route.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }
}
