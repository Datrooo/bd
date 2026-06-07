package ru.autoenterprise.repair

import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.RepairStatus
import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.employee.EmployeeRepository
import ru.autoenterprise.employee.EmployeeService
import ru.autoenterprise.organization.BrigadeRepository
import ru.autoenterprise.organization.SectionRepository
import ru.autoenterprise.organization.WorkshopRepository
import ru.autoenterprise.vehicle.VehicleEntity
import ru.autoenterprise.vehicle.VehicleRepository

@Service
@Transactional(readOnly = true)
class RepairTypeService(
    private val repairTypeRepository: RepairTypeRepository,
) {

    fun listTypes(): List<RepairTypeRow> =
        repairTypeRepository.findAllByOrderByNameAsc().map { repairType ->
            RepairTypeRow(repairType.id!!, repairType.name, repairType.description)
        }

    fun typeOptions(): List<RepairTypeOption> =
        repairTypeRepository.findAllByOrderByNameAsc().map { RepairTypeOption(it.id!!, it.name) }

    fun getTypeForm(id: Long): RepairTypeForm {
        val repairType = repairTypeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Тип ремонта не найден.") }

        return RepairTypeForm(
            id = repairType.id,
            name = repairType.name,
            description = repairType.description,
        )
    }

    @Transactional
    fun createType(form: RepairTypeForm) {
        val repairType = RepairTypeEntity()
        applyType(repairType, form)
        repairTypeRepository.save(repairType)
    }

    @Transactional
    fun updateType(id: Long, form: RepairTypeForm) {
        val repairType = repairTypeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Тип ремонта не найден.") }

        applyType(repairType, form)
    }

    @Transactional
    fun deleteType(id: Long) {
        repairTypeRepository.deleteById(id)
    }

    private fun applyType(repairType: RepairTypeEntity, form: RepairTypeForm) {
        repairType.name = form.name.trim()
        repairType.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }
}

@Service
@Transactional(readOnly = true)
class RepairService(
    private val repairRepository: RepairRepository,
    private val vehicleRepository: VehicleRepository,
    private val repairTypeRepository: RepairTypeRepository,
    private val workshopRepository: WorkshopRepository,
    private val sectionRepository: SectionRepository,
    private val brigadeRepository: BrigadeRepository,
) {

    fun listRepairs(page: Int): Page<RepairRow> =
        repairRepository.findAllBy(pageable(page, "startDate").withSort(Sort.by("startDate").descending())).map { repair ->
            RepairRow(
                id = repair.id!!,
                vehicleLabel = vehicleLabel(repair.vehicle),
                repairTypeName = repair.repairType.name,
                status = repair.status.name,
                startDate = repair.startDate,
                endDate = repair.endDate,
                totalCost = repair.totalCost,
            )
        }

    fun repairOptions(): List<RepairOption> =
        repairRepository.findAll(Sort.by("startDate").descending()).map { repair ->
            val period = repair.endDate?.let { endDate -> "${repair.startDate} - $endDate" }
                ?: "с ${repair.startDate}"
            RepairOption(repair.id!!, "#${repair.id} / ${vehicleLabel(repair.vehicle)} / $period")
        }

    fun getRepairForm(id: Long): RepairForm {
        val repair = repairRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Ремонт не найден.") }

        return RepairForm(
            id = repair.id,
            vehicleId = repair.vehicle.id,
            repairTypeId = repair.repairType.id,
            workshopId = repair.workshop?.id,
            sectionId = repair.section?.id,
            brigadeId = repair.brigade?.id,
            startDate = repair.startDate,
            endDate = repair.endDate,
            reason = repair.reason,
            description = repair.description,
            status = repair.status,
            notes = repair.notes,
        )
    }

    @Transactional
    fun createRepair(form: RepairForm) {
        val repair = RepairEntity()
        applyRepair(repair, form)
        repairRepository.save(repair)
    }

    @Transactional
    fun updateRepair(id: Long, form: RepairForm) {
        val repair = repairRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Ремонт не найден.") }

        applyRepair(repair, form)
    }

    @Transactional
    fun deleteRepair(id: Long) {
        repairRepository.deleteById(id)
    }

    companion object {
        val repairStatuses: List<RepairStatus> = RepairStatus.entries
    }

    private fun applyRepair(repair: RepairEntity, form: RepairForm) {
        repair.vehicle = findVehicle(form.vehicleId)
        repair.repairType = findRepairType(form.repairTypeId)
        repair.workshop = form.workshopId?.let { workshopId ->
            workshopRepository.findById(workshopId)
                .orElseThrow { EntityNotFoundException("Цех не найден.") }
        }
        repair.section = form.sectionId?.let { sectionId ->
            sectionRepository.findById(sectionId)
                .orElseThrow { EntityNotFoundException("Участок не найден.") }
        }
        repair.brigade = form.brigadeId?.let { brigadeId ->
            brigadeRepository.findById(brigadeId)
                .orElseThrow { EntityNotFoundException("Бригада не найдена.") }
        }
        repair.startDate = form.startDate ?: LocalDate.now()
        repair.endDate = form.endDate
        repair.reason = form.reason?.trim().takeUnless { it.isNullOrBlank() }
        repair.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
        repair.status = form.status
        repair.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun findRepairType(id: Long?): RepairTypeEntity {
        val repairTypeId = id ?: throw EntityNotFoundException("Тип ремонта не найден.")
        return repairTypeRepository.findById(repairTypeId)
            .orElseThrow { EntityNotFoundException("Тип ремонта не найден.") }
    }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

@Service
@Transactional(readOnly = true)
class RepairWorkService(
    private val repairWorkRepository: RepairWorkRepository,
    private val repairRepository: RepairRepository,
    private val employeeRepository: EmployeeRepository,
) {

    fun listWorks(page: Int): Page<RepairWorkRow> =
        repairWorkRepository.findAllBy(pageable(page, "completedAt").withSort(Sort.by("completedAt").descending().and(Sort.by("id").descending()))).map { repairWork ->
            RepairWorkRow(
                id = repairWork.id!!,
                repairLabel = "#${repairWork.repair.id} / ${repairWork.repair.vehicle.inventoryNumber}",
                employeeName = repairWork.employee?.let(EmployeeService::fullName),
                workType = repairWork.workType,
                quantity = repairWork.quantity,
                cost = repairWork.cost,
                completedAt = repairWork.completedAt,
            )
        }

    fun getWorkForm(id: Long): RepairWorkForm {
        val repairWork = repairWorkRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Ремонтная работа не найдена.") }

        return RepairWorkForm(
            id = repairWork.id,
            repairId = repairWork.repair.id,
            employeeId = repairWork.employee?.id,
            workType = repairWork.workType,
            description = repairWork.description,
            quantity = repairWork.quantity,
            cost = repairWork.cost,
            completedAt = repairWork.completedAt,
            notes = repairWork.notes,
        )
    }

    @Transactional
    fun createWork(form: RepairWorkForm) {
        val repairWork = RepairWorkEntity()
        applyWork(repairWork, form)
        repairWorkRepository.save(repairWork)
    }

    @Transactional
    fun updateWork(id: Long, form: RepairWorkForm) {
        val repairWork = repairWorkRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Ремонтная работа не найдена.") }

        applyWork(repairWork, form)
    }

    @Transactional
    fun deleteWork(id: Long) {
        repairWorkRepository.deleteById(id)
    }

    private fun applyWork(repairWork: RepairWorkEntity, form: RepairWorkForm) {
        repairWork.repair = findRepair(form.repairId)
        repairWork.employee = form.employeeId?.let { employeeId ->
            employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }
        }
        repairWork.workType = form.workType.trim()
        repairWork.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
        repairWork.quantity = form.quantity ?: BigDecimal.ONE
        repairWork.cost = form.cost ?: BigDecimal.ZERO
        repairWork.completedAt = form.completedAt
        repairWork.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findRepair(id: Long?): RepairEntity {
        val repairId = id ?: throw EntityNotFoundException("Ремонт не найден.")
        return repairRepository.findById(repairId)
            .orElseThrow { EntityNotFoundException("Ремонт не найден.") }
    }
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())
