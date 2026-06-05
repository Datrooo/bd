package ru.autoenterprise.organization

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
import java.time.LocalDate
import ru.autoenterprise.domain.VehicleDriverAssignmentType
import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.vehicle.VehicleEntity

@Entity
@Table(name = "workshop")
class WorkshopEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 150)
    var name: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chief_employee_id")
    var chiefEmployee: EmployeeEntity? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "section")
class SectionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workshop_id", nullable = false)
    var workshop: WorkshopEntity = WorkshopEntity(),
    @Column(nullable = false, length = 150)
    var name: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_employee_id")
    var masterEmployee: EmployeeEntity? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "brigade")
class BrigadeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    var section: SectionEntity = SectionEntity(),
    @Column(nullable = false, length = 150)
    var name: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brigadier_employee_id")
    var brigadierEmployee: EmployeeEntity? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "employee_brigade_assignment")
class EmployeeBrigadeAssignmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: EmployeeEntity = EmployeeEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brigade_id", nullable = false)
    var brigade: BrigadeEntity = BrigadeEntity(),
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(name = "is_primary", nullable = false)
    var primary: Boolean = true,
)

@Entity
@Table(name = "vehicle_driver_assignment")
class VehicleDriverAssignmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_employee_id", nullable = false)
    var driverEmployee: EmployeeEntity = EmployeeEntity(),
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 50)
    var assignmentType: VehicleDriverAssignmentType = VehicleDriverAssignmentType.PRIMARY,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
