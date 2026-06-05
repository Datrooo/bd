package ru.autoenterprise.route

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import ru.autoenterprise.domain.RouteType

@Entity
@Table(name = "route")
class RouteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "route_number", nullable = false, length = 20)
    var routeNumber: String = "",
    @Column(nullable = false, length = 150)
    var name: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 50)
    var routeType: RouteType = RouteType.BUS,
    @Column(name = "start_point", nullable = false, length = 150)
    var startPoint: String = "",
    @Column(name = "end_point", nullable = false, length = 150)
    var endPoint: String = "",
    @Column(name = "length_km", precision = 10, scale = 2)
    var lengthKm: BigDecimal? = null,
    @Column(name = "is_active", nullable = false)
    var active: Boolean = true,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
