package ru.autoenterprise.employee

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.EmployeePosition
import ru.autoenterprise.domain.EmployeeStatus

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
) {

    fun listEmployees(page: Int): Page<EmployeeRow> =
        employeeRepository.findAllBy(pageable(page)).map { employee ->
            EmployeeRow(
                id = employee.id!!,
                personnelNumber = employee.personnelNumber,
                fullName = fullName(employee),
                position = EmployeePosition.displayNameFor(employee.position),
                status = employee.status.name,
                hireDate = employee.hireDate,
            )
        }

    fun employeeOptions(): List<EmployeeOption> =
        employeeRepository.findAll(Sort.by("lastName").ascending().and(Sort.by("firstName").ascending()))
            .map { employee ->
                EmployeeOption(employee.id!!, fullName(employee))
            }

    fun driverOptions(): List<EmployeeOption> =
        employeeRepository.findAllByPositionIgnoreCaseOrderByLastNameAscFirstNameAsc(EmployeePosition.DRIVER.name)
            .map { employee ->
                EmployeeOption(employee.id!!, fullName(employee))
            }

    fun getEmployeeForm(id: Long): EmployeeForm {
        val employee = employeeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }

        return EmployeeForm(
            id = employee.id,
            personnelNumber = employee.personnelNumber,
            lastName = employee.lastName,
            firstName = employee.firstName,
            middleName = employee.middleName,
            birthDate = employee.birthDate,
            hireDate = employee.hireDate,
            dismissalDate = employee.dismissalDate,
            position = EmployeePosition.fromCode(employee.position)?.name ?: employee.position,
            qualification = employee.qualification,
            phone = employee.phone,
            email = employee.email,
            address = employee.address,
            status = employee.status,
            notes = employee.notes,
        )
    }

    @Transactional
    fun createEmployee(form: EmployeeForm) {
        val employee = EmployeeEntity()
        applyEmployee(employee, form)
        employeeRepository.save(employee)
    }

    @Transactional
    fun updateEmployee(id: Long, form: EmployeeForm) {
        val employee = employeeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Сотрудник не найден.") }

        applyEmployee(employee, form)
    }

    @Transactional
    fun deleteEmployee(id: Long) {
        employeeRepository.deleteById(id)
    }

    companion object {
        val employeeStatuses: List<EmployeeStatus> = EmployeeStatus.entries
        val employeePositions: List<EmployeePosition> = EmployeePosition.entries

        private const val pageSize = 10

        private fun pageable(page: Int): PageRequest =
            PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by("lastName").ascending().and(Sort.by("firstName").ascending()))

        fun fullName(employee: EmployeeEntity): String =
            listOfNotNull(employee.lastName, employee.firstName, employee.middleName)
                .joinToString(" ")
    }

    private fun applyEmployee(employee: EmployeeEntity, form: EmployeeForm) {
        employee.personnelNumber = form.personnelNumber.trim()
        employee.lastName = form.lastName.trim()
        employee.firstName = form.firstName.trim()
        employee.middleName = form.middleName?.trim().takeUnless { it.isNullOrBlank() }
        employee.birthDate = form.birthDate
        employee.hireDate = form.hireDate!!
        employee.dismissalDate = form.dismissalDate
        employee.position = normalizePosition(form.position).name
        employee.qualification = form.qualification?.trim().takeUnless { it.isNullOrBlank() }
        employee.phone = form.phone?.trim().takeUnless { it.isNullOrBlank() }
        employee.email = form.email?.trim().takeUnless { it.isNullOrBlank() }
        employee.address = form.address?.trim().takeUnless { it.isNullOrBlank() }
        employee.status = form.status
        employee.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun normalizePosition(position: String): EmployeePosition =
        EmployeePosition.fromCode(position)
            ?: throw IllegalArgumentException("Выберите должность из списка.")
}
