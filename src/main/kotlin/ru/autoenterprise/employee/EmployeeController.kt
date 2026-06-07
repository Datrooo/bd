package ru.autoenterprise.employee

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.dao.DataAccessException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class EmployeeController(
    private val employeeService: EmployeeService,
) {

    @GetMapping("/employees")
    fun listEmployees(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("page", employeeService.listEmployees(page))
        model.addAttribute("pagePath", "/employees")
        return "employee/list"
    }

    @GetMapping("/employees/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun newEmployee(model: Model): String {
        populateForm(model, EmployeeForm(hireDate = java.time.LocalDate.now()), true)
        return "employee/form"
    }

    @PostMapping("/employees")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun createEmployee(
        @Valid @ModelAttribute("employee") form: EmployeeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "employee/form"
        }

        return try {
            employeeService.createEmployee(form)
            redirectAttributes.addFlashAttribute("successMessage", "Сотрудник добавлен.")
            "redirect:/employees"
        } catch (_: DataAccessException) {
            bindingResult.reject("employee.save", "Не удалось сохранить сотрудника. Проверьте табельный номер и даты.")
            populateForm(model, form, true)
            "employee/form"
        }
    }

    @GetMapping("/employees/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun editEmployee(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            populateForm(model, employeeService.getEmployeeForm(id), false)
            "employee/form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Сотрудник не найден.")
            "redirect:/employees"
        }

    @PostMapping("/employees/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun updateEmployee(
        @PathVariable id: Long,
        @Valid @ModelAttribute("employee") form: EmployeeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "employee/form"
        }

        return try {
            employeeService.updateEmployee(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Сотрудник обновлен.")
            "redirect:/employees"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("employee.update", "Сотрудник не найден.")
            populateForm(model, form.copy(id = id), false)
            "employee/form"
        } catch (_: DataAccessException) {
            bindingResult.reject("employee.update", "Не удалось обновить сотрудника. Проверьте табельный номер и даты.")
            populateForm(model, form.copy(id = id), false)
            "employee/form"
        }
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun deleteEmployee(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            employeeService.deleteEmployee(id)
            redirectAttributes.addFlashAttribute("successMessage", "Сотрудник удален.")
            "redirect:/employees"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить сотрудника, если на него есть ссылки в других разделах.")
            "redirect:/employees"
        }

    private fun populateForm(model: Model, form: EmployeeForm, creating: Boolean) {
        model.addAttribute("employee", form)
        model.addAttribute("statuses", EmployeeService.employeeStatuses)
        model.addAttribute("pageTitle", if (creating) "Новый сотрудник" else "Редактирование сотрудника")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/employees" else "/employees/${form.id}")
    }
}
