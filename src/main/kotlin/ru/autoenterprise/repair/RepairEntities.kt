package ru.autoenterprise.repair

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
import java.time.LocalDateTime
import ru.autoenterprise.domain.RepairStatus
import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.organization.BrigadeEntity
import ru.autoenterprise.organization.SectionEntity
import ru.autoenterprise.organization.WorkshopEntity
import ru.autoenterprise.vehicle.VehicleEntity

@Entity
@Table(name = "repair_type")
class RepairTypeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 100)
    var name: String = "",
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "repair")
class RepairEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_type_id", nullable = false)
    var repairType: RepairTypeEntity = RepairTypeEntity(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    var workshop: WorkshopEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    var section: SectionEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brigade_id")
    var brigade: BrigadeEntity? = null,
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(columnDefinition = "text")
    var reason: String? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
    @Column(name = "total_cost", nullable = false, precision = 14, scale = 2)
    var totalCost: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: RepairStatus = RepairStatus.PLANNED,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)

@Entity
@Table(name = "repair_work")
class RepairWorkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_id", nullable = false)
    var repair: RepairEntity = RepairEntity(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: EmployeeEntity? = null,
    @Column(name = "work_type", nullable = false, length = 150)
    var workType: String = "",
    @Column(columnDefinition = "text")
    var description: String? = null,
    @Column(nullable = false, precision = 12, scale = 2)
    var quantity: BigDecimal = BigDecimal.ONE,
    @Column(nullable = false, precision = 14, scale = 2)
    var cost: BigDecimal = BigDecimal.ZERO,
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
