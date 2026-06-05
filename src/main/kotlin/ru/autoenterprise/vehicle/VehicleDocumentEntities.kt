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
import ru.autoenterprise.domain.VehicleAcquisitionType
import ru.autoenterprise.domain.VehicleDisposalType

@Entity
@Table(name = "vehicle_acquisition")
class VehicleAcquisitionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @Column(name = "acquisition_date", nullable = false)
    var acquisitionDate: LocalDate = LocalDate.now(),
    @Enumerated(EnumType.STRING)
    @Column(name = "acquisition_type", nullable = false, length = 50)
    var acquisitionType: VehicleAcquisitionType = VehicleAcquisitionType.PURCHASE,
    @Column(name = "supplier_name", length = 150)
    var supplierName: String? = null,
    @Column(name = "document_number", length = 100)
    var documentNumber: String? = null,
    @Column(precision = 14, scale = 2)
    var cost: BigDecimal? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)

@Entity
@Table(name = "vehicle_disposal")
class VehicleDisposalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity = VehicleEntity(),
    @Column(name = "disposal_date", nullable = false)
    var disposalDate: LocalDate = LocalDate.now(),
    @Enumerated(EnumType.STRING)
    @Column(name = "disposal_type", nullable = false, length = 50)
    var disposalType: VehicleDisposalType = VehicleDisposalType.WRITE_OFF,
    @Column(columnDefinition = "text")
    var reason: String? = null,
    @Column(name = "document_number", length = 100)
    var documentNumber: String? = null,
    @Column(name = "amount_received", precision = 14, scale = 2)
    var amountReceived: BigDecimal? = null,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
