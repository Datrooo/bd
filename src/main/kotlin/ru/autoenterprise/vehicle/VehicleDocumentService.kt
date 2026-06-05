package ru.autoenterprise.vehicle

import jakarta.persistence.EntityNotFoundException
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.VehicleAcquisitionType
import ru.autoenterprise.domain.VehicleDisposalType

@Service
@Transactional(readOnly = true)
class VehicleAcquisitionService(
    private val vehicleAcquisitionRepository: VehicleAcquisitionRepository,
    private val vehicleRepository: VehicleRepository,
) {

    fun listAcquisitions(page: Int): Page<VehicleAcquisitionRow> =
        vehicleAcquisitionRepository.findAllBy(pageable(page, "acquisitionDate").withSort(Sort.by("acquisitionDate").descending())).map { acquisition ->
            VehicleAcquisitionRow(
                id = acquisition.id!!,
                vehicleLabel = vehicleLabel(acquisition.vehicle),
                acquisitionDate = acquisition.acquisitionDate,
                acquisitionType = acquisition.acquisitionType.name,
                supplierName = acquisition.supplierName,
                cost = acquisition.cost,
            )
        }

    fun getAcquisitionForm(id: Long): VehicleAcquisitionForm {
        val acquisition = vehicleAcquisitionRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Документ поступления не найден.") }

        return VehicleAcquisitionForm(
            id = acquisition.id,
            vehicleId = acquisition.vehicle.id,
            acquisitionDate = acquisition.acquisitionDate,
            acquisitionType = acquisition.acquisitionType,
            supplierName = acquisition.supplierName,
            documentNumber = acquisition.documentNumber,
            cost = acquisition.cost,
            notes = acquisition.notes,
        )
    }

    @Transactional
    fun createAcquisition(form: VehicleAcquisitionForm) {
        val acquisition = VehicleAcquisitionEntity()
        applyAcquisition(acquisition, form)
        vehicleAcquisitionRepository.save(acquisition)
    }

    @Transactional
    fun updateAcquisition(id: Long, form: VehicleAcquisitionForm) {
        val acquisition = vehicleAcquisitionRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Документ поступления не найден.") }

        applyAcquisition(acquisition, form)
    }

    @Transactional
    fun deleteAcquisition(id: Long) {
        vehicleAcquisitionRepository.deleteById(id)
    }

    companion object {
        val acquisitionTypes: List<VehicleAcquisitionType> = VehicleAcquisitionType.entries
    }

    private fun applyAcquisition(acquisition: VehicleAcquisitionEntity, form: VehicleAcquisitionForm) {
        acquisition.vehicle = findVehicle(form.vehicleId)
        acquisition.acquisitionDate = form.acquisitionDate ?: LocalDate.now()
        acquisition.acquisitionType = form.acquisitionType
        acquisition.supplierName = form.supplierName?.trim().takeUnless { it.isNullOrBlank() }
        acquisition.documentNumber = form.documentNumber?.trim().takeUnless { it.isNullOrBlank() }
        acquisition.cost = form.cost
        acquisition.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

@Service
@Transactional(readOnly = true)
class VehicleDisposalService(
    private val vehicleDisposalRepository: VehicleDisposalRepository,
    private val vehicleRepository: VehicleRepository,
) {

    fun listDisposals(page: Int): Page<VehicleDisposalRow> =
        vehicleDisposalRepository.findAllBy(pageable(page, "disposalDate").withSort(Sort.by("disposalDate").descending())).map { disposal ->
            VehicleDisposalRow(
                id = disposal.id!!,
                vehicleLabel = vehicleLabel(disposal.vehicle),
                disposalDate = disposal.disposalDate,
                disposalType = disposal.disposalType.name,
                amountReceived = disposal.amountReceived,
            )
        }

    fun getDisposalForm(id: Long): VehicleDisposalForm {
        val disposal = vehicleDisposalRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Документ выбытия не найден.") }

        return VehicleDisposalForm(
            id = disposal.id,
            vehicleId = disposal.vehicle.id,
            disposalDate = disposal.disposalDate,
            disposalType = disposal.disposalType,
            reason = disposal.reason,
            documentNumber = disposal.documentNumber,
            amountReceived = disposal.amountReceived,
            notes = disposal.notes,
        )
    }

    @Transactional
    fun createDisposal(form: VehicleDisposalForm) {
        val disposal = VehicleDisposalEntity()
        applyDisposal(disposal, form)
        vehicleDisposalRepository.save(disposal)
    }

    @Transactional
    fun updateDisposal(id: Long, form: VehicleDisposalForm) {
        val disposal = vehicleDisposalRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Документ выбытия не найден.") }

        applyDisposal(disposal, form)
    }

    @Transactional
    fun deleteDisposal(id: Long) {
        vehicleDisposalRepository.deleteById(id)
    }

    companion object {
        val disposalTypes: List<VehicleDisposalType> = VehicleDisposalType.entries
    }

    private fun applyDisposal(disposal: VehicleDisposalEntity, form: VehicleDisposalForm) {
        disposal.vehicle = findVehicle(form.vehicleId)
        disposal.disposalDate = form.disposalDate ?: LocalDate.now()
        disposal.disposalType = form.disposalType
        disposal.reason = form.reason?.trim().takeUnless { it.isNullOrBlank() }
        disposal.documentNumber = form.documentNumber?.trim().takeUnless { it.isNullOrBlank() }
        disposal.amountReceived = form.amountReceived
        disposal.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())
