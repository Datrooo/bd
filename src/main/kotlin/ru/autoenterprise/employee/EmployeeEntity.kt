package ru.autoenterprise.employee

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import ru.autoenterprise.domain.EmployeeStatus

@Entity
@Table(name = "employee")
class EmployeeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "personnel_number", nullable = false, unique = true, length = 50)
    var personnelNumber: String = "",
    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",
    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",
    @Column(name = "middle_name", length = 100)
    var middleName: String? = null,
    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,
    @Column(name = "hire_date", nullable = false)
    var hireDate: LocalDate = LocalDate.now(),
    @Column(name = "dismissal_date")
    var dismissalDate: LocalDate? = null,
    @Column(nullable = false, length = 100)
    var position: String = "",
    @Column(length = 200)
    var qualification: String? = null,
    @Column(length = 30)
    var phone: String? = null,
    @Column(length = 150)
    var email: String? = null,
    @Column(columnDefinition = "text")
    var address: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: EmployeeStatus = EmployeeStatus.ACTIVE,
    @Column(columnDefinition = "text")
    var notes: String? = null,
)
