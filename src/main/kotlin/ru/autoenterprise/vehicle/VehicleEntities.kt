package ru.autoenterprise.vehicle

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
import ru.autoenterprise.domain.VehicleStatus

@Entity
@Table(name = "vehicle_category")
class VehicleCategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 100)
    var name: String = "",
    @Column(columnDefinition = "text")
    var description: String? = null,
)

@Entity
@Table(name = "vehicle")
class VehicleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "inventory_number", nullable = false, unique = true, length = 50)
    var inventoryNumber: String = "",
    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    var registrationNumber: String = "",
    @Column(unique = true, length = 50)
    var vin: String? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: VehicleCategoryEntity = VehicleCategoryEntity(),
    @Column(name = "brand_name", nullable = false, length = 100)
    var brandName: String = "",
    @Column(name = "model_name", nullable = false, length = 100)
    var modelName: String = "",
    @Column(name = "manufacture_year")
    var manufactureYear: Int? = null,
    @Column(name = "purchase_date")
    var purchaseDate: LocalDate? = null,
    @Column(name = "commissioning_date")
    var commissioningDate: LocalDate? = null,
    @Column(name = "current_mileage", nullable = false, precision = 12, scale = 2)
    var currentMileage: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: VehicleStatus = VehicleStatus.ACTIVE,
    @Column(name = "passenger_capacity")
    var passengerCapacity: Int? = null,
    @Column(name = "load_capacity_kg", precision = 12, scale = 2)
    var loadCapacityKg: BigDecimal? = null,
    @Column(name = "cargo_volume_m3", precision = 12, scale = 2)
    var cargoVolumeM3: BigDecimal? = null,
    @Column(name = "body_type", length = 100)
    var bodyType: String? = null,
    @Column(name = "taxi_license_number", length = 100)
    var taxiLicenseNumber: String? = null,
    @Column(name = "service_purpose", length = 200)
    var servicePurpose: String? = null,
    @Column(length = 50)
    var color: String? = null,
    @Column(name = "engine_number", length = 50)
    var engineNumber: String? = null,
    @Column(name = "chassis_number", length = 50)
    var chassisNumber: String? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
