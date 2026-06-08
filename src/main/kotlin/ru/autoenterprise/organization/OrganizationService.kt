package ru.autoenterprise.organization

import jakarta.persistence.EntityNotFoundException
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.EmployeePosition
import ru.autoenterprise.domain.VehicleDriverAssignmentType
import ru.autoenterprise.employee.EmployeeEntity
import ru.autoenterprise.employee.EmployeeRepository
import ru.autoenterprise.employee.EmployeeService
import ru.autoenterprise.vehicle.VehicleEntity
import ru.autoenterprise.vehicle.VehicleRepository

@Service
@Transactional(readOnly = true)
class WorkshopService(
    private val workshopRepository: WorkshopRepository,
    private val employeeRepository: EmployeeRepository,
) {

    fun listWorkshops(page: Int): Page<WorkshopRow> =
        workshopRepository.findAllBy(pageable(page, "name")).map { workshop ->
            WorkshopRow(
                id = workshop.id!!,
                name = workshop.name,
                chiefEmployeeName = workshop.chiefEmployee?.let(EmployeeService::fullName),
            )
        }

    fun workshopOptions(): List<WorkshopOption> =
        workshopRepository.findAllByOrderByNameAsc().map { WorkshopOption(it.id!!, it.name) }

    fun getWorkshopForm(id: Long): WorkshopForm {
        val workshop = workshopRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Цех не найден.") }

        return WorkshopForm(
            id = workshop.id,
            name = workshop.name,
            chiefEmployeeId = workshop.chiefEmployee?.id,
            description = workshop.description,
        )
    }

    @Transactional
    fun createWorkshop(form: WorkshopForm) {
        val workshop = WorkshopEntity()
        applyWorkshop(workshop, form)
        workshopRepository.save(workshop)
    }

    @Transactional
    fun updateWorkshop(id: Long, form: WorkshopForm) {
        val workshop = workshopRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Цех не найден.") }

        applyWorkshop(workshop, form)
    }

    @Transactional
    fun deleteWorkshop(id: Long) {
        workshopRepository.deleteById(id)
    }

    private fun applyWorkshop(workshop: WorkshopEntity, form: WorkshopForm) {
        workshop.name = form.name.trim()
        workshop.chiefEmployee = form.chiefEmployeeId?.let(::findEmployee)
        workshop.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findEmployee(id: Long): EmployeeEntity =
        employeeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }
}

@Service
@Transactional(readOnly = true)
class SectionManagementService(
    private val sectionRepository: SectionRepository,
    private val workshopRepository: WorkshopRepository,
    private val employeeRepository: EmployeeRepository,
) {

    fun listSections(page: Int): Page<SectionRow> =
        sectionRepository.findAllBy(pageable(page, "name")).map { section ->
            SectionRow(
                id = section.id!!,
                name = section.name,
                workshopName = section.workshop.name,
                masterEmployeeName = section.masterEmployee?.let(EmployeeService::fullName),
            )
        }

    fun sectionOptions(): List<SectionOption> =
        sectionRepository.findAllByOrderByNameAsc().map { section ->
            SectionOption(section.id!!, "${section.workshop.name} / ${section.name}")
        }

    fun getSectionForm(id: Long): SectionForm {
        val section = sectionRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Участок не найден.") }

        return SectionForm(
            id = section.id,
            workshopId = section.workshop.id,
            name = section.name,
            masterEmployeeId = section.masterEmployee?.id,
            description = section.description,
        )
    }

    @Transactional
    fun createSection(form: SectionForm) {
        val section = SectionEntity()
        applySection(section, form)
        sectionRepository.save(section)
    }

    @Transactional
    fun updateSection(id: Long, form: SectionForm) {
        val section = sectionRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Участок не найден.") }

        applySection(section, form)
    }

    @Transactional
    fun deleteSection(id: Long) {
        sectionRepository.deleteById(id)
    }

    private fun applySection(section: SectionEntity, form: SectionForm) {
        val workshopId = form.workshopId ?: throw EntityNotFoundException("Цех не найден.")

        section.workshop = workshopRepository.findById(workshopId)
            .orElseThrow { EntityNotFoundException("Цех не найден.") }
        section.name = form.name.trim()
        section.masterEmployee = form.masterEmployeeId?.let(::findEmployee)
        section.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findEmployee(id: Long): EmployeeEntity =
        employeeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }
}

@Service
@Transactional(readOnly = true)
class BrigadeService(
    private val brigadeRepository: BrigadeRepository,
    private val sectionRepository: SectionRepository,
    private val employeeRepository: EmployeeRepository,
) {

    fun listBrigades(page: Int): Page<BrigadeRow> =
        brigadeRepository.findAllBy(pageable(page, "name")).map { brigade ->
            BrigadeRow(
                id = brigade.id!!,
                name = brigade.name,
                sectionName = brigade.section.name,
                workshopName = brigade.section.workshop.name,
                brigadierEmployeeName = brigade.brigadierEmployee?.let(EmployeeService::fullName),
            )
        }

    fun brigadeOptions(): List<BrigadeOption> =
        brigadeRepository.findAllByOrderByNameAsc().map { brigade ->
            BrigadeOption(brigade.id!!, "${brigade.section.name} / ${brigade.name}")
        }

    fun getBrigadeForm(id: Long): BrigadeForm {
        val brigade = brigadeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Бригада не найдена.") }

        return BrigadeForm(
            id = brigade.id,
            sectionId = brigade.section.id,
            name = brigade.name,
            brigadierEmployeeId = brigade.brigadierEmployee?.id,
            description = brigade.description,
        )
    }

    @Transactional
    fun createBrigade(form: BrigadeForm) {
        val brigade = BrigadeEntity()
        applyBrigade(brigade, form)
        brigadeRepository.save(brigade)
    }

    @Transactional
    fun updateBrigade(id: Long, form: BrigadeForm) {
        val brigade = brigadeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Бригада не найдена.") }

        applyBrigade(brigade, form)
    }

    @Transactional
    fun deleteBrigade(id: Long) {
        brigadeRepository.deleteById(id)
    }

    private fun applyBrigade(brigade: BrigadeEntity, form: BrigadeForm) {
        val sectionId = form.sectionId ?: throw EntityNotFoundException("Участок не найден.")

        brigade.section = sectionRepository.findById(sectionId)
            .orElseThrow { EntityNotFoundException("Участок не найден.") }
        brigade.name = form.name.trim()
        brigade.brigadierEmployee = form.brigadierEmployeeId?.let(::findEmployee)
        brigade.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findEmployee(id: Long): EmployeeEntity =
        employeeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }
}

@Service
@Transactional(readOnly = true)
class EmployeeBrigadeAssignmentService(
    private val assignmentRepository: EmployeeBrigadeAssignmentRepository,
    private val employeeRepository: EmployeeRepository,
    private val brigadeRepository: BrigadeRepository,
) {

    fun listAssignments(page: Int): Page<EmployeeBrigadeAssignmentRow> =
        assignmentRepository.findAllBy(pageable(page, "startDate").withSort(Sort.by("startDate").descending())).map { assignment ->
            EmployeeBrigadeAssignmentRow(
                id = assignment.id!!,
                employeeName = EmployeeService.fullName(assignment.employee),
                brigadeName = assignment.brigade.name,
                sectionName = assignment.brigade.section.name,
                startDate = assignment.startDate,
                endDate = assignment.endDate,
                primary = assignment.primary,
            )
        }

    fun getAssignmentForm(id: Long): EmployeeBrigadeAssignmentForm {
        val assignment = assignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение сотрудника в бригаду не найдено.") }

        return EmployeeBrigadeAssignmentForm(
            id = assignment.id,
            employeeId = assignment.employee.id,
            brigadeId = assignment.brigade.id,
            startDate = assignment.startDate,
            endDate = assignment.endDate,
            primary = assignment.primary,
        )
    }

    @Transactional
    fun createAssignment(form: EmployeeBrigadeAssignmentForm) {
        val assignment = EmployeeBrigadeAssignmentEntity()
        applyAssignment(assignment, form)
        assignmentRepository.save(assignment)
    }

    @Transactional
    fun updateAssignment(id: Long, form: EmployeeBrigadeAssignmentForm) {
        val assignment = assignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение сотрудника в бригаду не найдено.") }

        applyAssignment(assignment, form)
    }

    @Transactional
    fun deleteAssignment(id: Long) {
        assignmentRepository.deleteById(id)
    }

    private fun applyAssignment(assignment: EmployeeBrigadeAssignmentEntity, form: EmployeeBrigadeAssignmentForm) {
        assignment.employee = findEmployee(form.employeeId)
        assignment.brigade = findBrigade(form.brigadeId)
        assignment.startDate = form.startDate ?: LocalDate.now()
        assignment.endDate = form.endDate
        assignment.primary = form.primary
    }

    private fun findEmployee(id: Long?): EmployeeEntity {
        val employeeId = id ?: throw EntityNotFoundException("Сотрудник не найден.")
        return employeeRepository.findById(employeeId)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }
    }

    private fun findBrigade(id: Long?): BrigadeEntity {
        val brigadeId = id ?: throw EntityNotFoundException("Бригада не найдена.")
        return brigadeRepository.findById(brigadeId)
            .orElseThrow { EntityNotFoundException("Бригада не найдена.") }
    }
}

@Service
@Transactional(readOnly = true)
class VehicleDriverAssignmentService(
    private val assignmentRepository: VehicleDriverAssignmentRepository,
    private val vehicleRepository: VehicleRepository,
    private val employeeRepository: EmployeeRepository,
) {

    fun listAssignments(page: Int): Page<VehicleDriverAssignmentRow> =
        assignmentRepository.findAllBy(pageable(page, "startDate").withSort(Sort.by("startDate").descending())).map { assignment ->
            VehicleDriverAssignmentRow(
                id = assignment.id!!,
                vehicleLabel = vehicleLabel(assignment.vehicle),
                driverName = EmployeeService.fullName(assignment.driverEmployee),
                assignmentType = assignment.assignmentType.name,
                startDate = assignment.startDate,
                endDate = assignment.endDate,
            )
        }

    fun getAssignmentForm(id: Long): VehicleDriverAssignmentForm {
        val assignment = assignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение водителя не найдено.") }

        return VehicleDriverAssignmentForm(
            id = assignment.id,
            vehicleId = assignment.vehicle.id,
            driverEmployeeId = assignment.driverEmployee.id,
            startDate = assignment.startDate,
            endDate = assignment.endDate,
            assignmentType = assignment.assignmentType,
            notes = assignment.notes,
        )
    }

    @Transactional
    fun createAssignment(form: VehicleDriverAssignmentForm) {
        val assignment = VehicleDriverAssignmentEntity()
        applyAssignment(assignment, form)
        assignmentRepository.save(assignment)
    }

    @Transactional
    fun updateAssignment(id: Long, form: VehicleDriverAssignmentForm) {
        val assignment = assignmentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Назначение водителя не найдено.") }

        applyAssignment(assignment, form)
    }

    @Transactional
    fun deleteAssignment(id: Long) {
        assignmentRepository.deleteById(id)
    }

    companion object {
        val assignmentTypes: List<VehicleDriverAssignmentType> = VehicleDriverAssignmentType.entries
    }

    private fun applyAssignment(assignment: VehicleDriverAssignmentEntity, form: VehicleDriverAssignmentForm) {
        assignment.vehicle = findVehicle(form.vehicleId)
        assignment.driverEmployee = findEmployee(form.driverEmployeeId)
        assignment.startDate = form.startDate ?: LocalDate.now()
        assignment.endDate = form.endDate
        assignment.assignmentType = form.assignmentType
        assignment.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun findEmployee(id: Long?): EmployeeEntity {
        val employeeId = id ?: throw EntityNotFoundException("Сотрудник не найден.")
        val employee = employeeRepository.findById(employeeId)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }

        if (!EmployeePosition.isDriver(employee.position)) {
            throw EntityNotFoundException("Сотрудник ${EmployeeService.fullName(employee)} не является водителем.")
        }

        return employee
    }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())

private fun PageRequest.withSort(sort: Sort): PageRequest =
    PageRequest.of(pageNumber, pageSize, sort)
