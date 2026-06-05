package ru.autoenterprise.transportation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import ru.autoenterprise.domain.TransportationRecordType
import ru.autoenterprise.route.RouteEntity
import ru.autoenterprise.vehicle.VehicleEntity

@Entity
@Table(name = "route_vehicle_assignment")
class RouteVehicleAssignmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    var route: RouteEntity = RouteEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(name = "shift_info", length = 100)
    var shiftInfo: String? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)

@Entity
@Table(name = "transportation_record")
class TransportationRecordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    var route: RouteEntity? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 50)
    var recordType: TransportationRecordType = TransportationRecordType.PASSENGER,
    @Column(name = "record_date", nullable = false)
    var recordDate: LocalDate = LocalDate.now(),
    @Column(name = "mileage_km", nullable = false, precision = 12, scale = 2)
    var mileageKm: BigDecimal = BigDecimal.ZERO,
    @Column(name = "hours_used", precision = 10, scale = 2)
    var hoursUsed: BigDecimal? = null,
    @Column(name = "passenger_count")
    var passengerCount: Int? = null,
    @Column(name = "cargo_weight_kg", precision = 12, scale = 2)
    var cargoWeightKg: BigDecimal? = null,
    @Column(name = "cargo_volume_m3", precision = 12, scale = 2)
    var cargoVolumeM3: BigDecimal? = null,
    @Column(name = "trip_count", nullable = false)
    var tripCount: Int = 1,
    @Column(name = "revenue", precision = 14, scale = 2)
    var revenue: BigDecimal? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
