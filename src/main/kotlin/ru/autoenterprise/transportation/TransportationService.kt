package ru.autoenterprise.transportation

import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.TransportationRecordType
import ru.autoenterprise.route.RouteEntity
import ru.autoenterprise.route.RouteRepository
import ru.autoenterprise.vehicle.VehicleEntity
import ru.autoenterprise.vehicle.VehicleRepository

@Service
@Transactional(readOnly = true)
class RouteVehicleAssignmentService(
    private val routeVehicleAssignmentRepository: RouteVehicleAssignmentRepository,
    private val routeRepository: RouteRepository,
    private val vehicleRepository: VehicleRepository,
) {

    fun listAssignments(page: Int): Page<RouteVehicleAssignmentRow> =
        routeVehicleAssignmentRepository.findAllBy(pageable(page, "startDate").withSort(Sort.by("startDate").descending())).map { assignment ->
            RouteVehicleAssignmentRow(
                id = assignment.id!!,
                routeLabel = "${assignment.route.routeNumber} / ${assignment.route.name}",
                vehicleLabel = vehicleLabel(assignment.vehicle),
                startDate = assignment.startDate,
                endDate = assignment.endDate,
                shiftInfo = assignment.shiftInfo,
            )
        }

    fun getAssignmentForm(id: Long): RouteVehicleAssignmentForm {
        val assignment = routeVehicleAssignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение транспорта на маршрут не найдено.") }

        return RouteVehicleAssignmentForm(
            id = assignment.id,
            routeId = assignment.route.id,
            vehicleId = assignment.vehicle.id,
            startDate = assignment.startDate,
            endDate = assignment.endDate,
            shiftInfo = assignment.shiftInfo,
            notes = assignment.notes,
        )
    }

    @Transactional
    fun createAssignment(form: RouteVehicleAssignmentForm) {
        val assignment = RouteVehicleAssignmentEntity()
        applyAssignment(assignment, form)
        routeVehicleAssignmentRepository.save(assignment)
    }

    @Transactional
    fun updateAssignment(id: Long, form: RouteVehicleAssignmentForm) {
        val assignment = routeVehicleAssignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение транспорта на маршрут не найдено.") }

        applyAssignment(assignment, form)
    }

    @Transactional
    fun deleteAssignment(id: Long) {
        routeVehicleAssignmentRepository.deleteById(id)
    }

    private fun applyAssignment(assignment: RouteVehicleAssignmentEntity, form: RouteVehicleAssignmentForm) {
        assignment.route = findRoute(form.routeId)
        assignment.vehicle = findVehicle(form.vehicleId)
        assignment.startDate = form.startDate ?: LocalDate.now()
        assignment.endDate = form.endDate
        assignment.shiftInfo = form.shiftInfo?.trim().takeUnless { it.isNullOrBlank() }
        assignment.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findRoute(id: Long?): RouteEntity {
        val routeId = id ?: throw EntityNotFoundException("Маршрут не найден.")
        return routeRepository.findById(routeId)
            .orElseThrow { EntityNotFoundException("Маршрут не найден.") }
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
class TransportationRecordService(
    private val transportationRecordRepository: TransportationRecordRepository,
    private val vehicleRepository: VehicleRepository,
    private val routeRepository: RouteRepository,
) {

    fun listRecords(page: Int): Page<TransportationRecordRow> =
        transportationRecordRepository.findAllBy(pageable(page, "recordDate").withSort(Sort.by("recordDate").descending())).map { record ->
            TransportationRecordRow(
                id = record.id!!,
                vehicleLabel = vehicleLabel(record.vehicle),
                routeLabel = record.route?.let { "${it.routeNumber} / ${it.name}" },
                recordType = record.recordType.name,
                recordDate = record.recordDate,
                mileageKm = record.mileageKm,
                tripCount = record.tripCount,
                revenue = record.revenue,
            )
        }

    fun getRecordForm(id: Long): TransportationRecordForm {
        val record = transportationRecordRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Эксплуатационная запись не найдена.") }

        return TransportationRecordForm(
            id = record.id,
            vehicleId = record.vehicle.id,
            routeId = record.route?.id,
            recordType = record.recordType,
            recordDate = record.recordDate,
            mileageKm = record.mileageKm,
            hoursUsed = record.hoursUsed,
            passengerCount = record.passengerCount,
            cargoWeightKg = record.cargoWeightKg,
            cargoVolumeM3 = record.cargoVolumeM3,
            tripCount = record.tripCount,
            revenue = record.revenue,
            description = record.description,
            notes = record.notes,
        )
    }

    @Transactional
    fun createRecord(form: TransportationRecordForm) {
        val record = TransportationRecordEntity()
        applyRecord(record, form)
        transportationRecordRepository.save(record)
    }

    @Transactional
    fun updateRecord(id: Long, form: TransportationRecordForm) {
        val record = transportationRecordRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Эксплуатационная запись не найдена.") }

        applyRecord(record, form)
    }

    @Transactional
    fun deleteRecord(id: Long) {
        transportationRecordRepository.deleteById(id)
    }

    companion object {
        val recordTypes: List<TransportationRecordType> = TransportationRecordType.entries
    }

    private fun applyRecord(record: TransportationRecordEntity, form: TransportationRecordForm) {
        record.vehicle = findVehicle(form.vehicleId)
        record.route = form.routeId?.let(::findRoute)
        record.recordType = form.recordType
        record.recordDate = form.recordDate ?: LocalDate.now()
        record.mileageKm = form.mileageKm ?: BigDecimal.ZERO
        record.hoursUsed = form.hoursUsed
        record.passengerCount = form.passengerCount
        record.cargoWeightKg = form.cargoWeightKg
        record.cargoVolumeM3 = form.cargoVolumeM3
        record.tripCount = form.tripCount ?: 1
        record.revenue = form.revenue
        record.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
        record.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun findRoute(id: Long): RouteEntity =
        routeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Маршрут не найден.") }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())
