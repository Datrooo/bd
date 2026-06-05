package ru.autoenterprise.component

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
import ru.autoenterprise.domain.ComponentStatus
import ru.autoenterprise.domain.VehicleComponentActionType
import ru.autoenterprise.repair.RepairEntity
import ru.autoenterprise.vehicle.VehicleEntity

@Entity
@Table(name = "component")
class ComponentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "component_type", nullable = false, length = 100)
    var componentType: String = "",
    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    var serialNumber: String = "",
    @Column(length = 100)
    var model: String? = null,
    @Column(length = 100)
    var manufacturer: String? = null,
    @Column(name = "production_date")
    var productionDate: LocalDate? = null,
    @Column(name = "purchase_date")
    var purchaseDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: ComponentStatus = ComponentStatus.IN_STOCK,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)

@Entity
@Table(name = "vehicle_component_history")
class VehicleComponentHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    var component: ComponentEntity = ComponentEntity(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id")
    var repair: RepairEntity? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    var actionType: VehicleComponentActionType = VehicleComponentActionType.INSTALLED,
    @Column(name = "action_date", nullable = false)
    var actionDate: LocalDate = LocalDate.now(),
    @Column(nullable = false, precision = 14, scale = 2)
    var cost: BigDecimal = BigDecimal.ZERO,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
