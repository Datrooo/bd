package ru.autoenterprise.garage

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
import ru.autoenterprise.domain.GarageObjectType
import ru.autoenterprise.organization.SectionEntity
import ru.autoenterprise.organization.WorkshopEntity
import ru.autoenterprise.vehicle.VehicleEntity

@Entity
@Table(name = "garage_object")
class GarageObjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, length = 150)
    var name: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    var objectType: GarageObjectType = GarageObjectType.GARAGE,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_object_id")
    var parentObject: GarageObjectEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    var workshop: WorkshopEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    var section: SectionEntity? = null,
    @Column(length = 255)
    var address: String? = null,
    @Column
    var capacity: Int? = null,
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "vehicle_location_history")
class VehicleLocationHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garage_object_id", nullable = false)
    var garageObject: GarageObjectEntity = GarageObjectEntity(),
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
