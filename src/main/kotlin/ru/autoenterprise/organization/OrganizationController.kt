package ru.autoenterprise.organization

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import java.time.LocalDate
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import ru.autoenterprise.employee.EmployeeService
import ru.autoenterprise.vehicle.VehicleService
import ru.autoenterprise.web.dataAccessErrorMessage

@Controller
class OrganizationController {

    @GetMapping("/organization")
    fun index(): String = "organization/index"
}

@Controller
@RequestMapping("/workshops")
class WorkshopController(
    private val workshopService: WorkshopService,
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", workshopService.listWorkshops(page))
        model.addAttribute("pagePath", "/workshops")
        return "organization/workshop-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, WorkshopForm(), true)
        return "organization/workshop-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("workshop") form: WorkshopForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "organization/workshop-form"
        }

        return try {
            workshopService.createWorkshop(form)
            redirectAttributes.addFlashAttribute("successMessage", "Цех добавлен.")
            "redirect:/workshops"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "workshop.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить цех. Проверьте уникальность названия."),
            )
            populateForm(model, form, true)
            "organization/workshop-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, workshopService.getWorkshopForm(id), false)
            "organization/workshop-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Цех не найден.")
            "redirect:/workshops"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("workshop") form: WorkshopForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "organization/workshop-form"
        }

        return try {
            workshopService.updateWorkshop(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Цех обновлен.")
            "redirect:/workshops"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("workshop.update", "Цех не найден.")
            populateForm(model, form.copy(id = id), false)
            "organization/workshop-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "workshop.update",
                dataAccessErrorMessage(ex, "Не удалось обновить цех. Проверьте уникальность названия."),
            )
            populateForm(model, form.copy(id = id), false)
            "organization/workshop-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            workshopService.deleteWorkshop(id)
            redirectAttributes.addFlashAttribute("successMessage", "Цех удален.")
            "redirect:/workshops"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить цех, если на него есть ссылки в участках, гаражах или ремонтах.")
            "redirect:/workshops"
        }

    private fun populateForm(model: Model, form: WorkshopForm, creating: Boolean) {
        model.addAttribute("workshop", form)
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("pageTitle", if (creating) "Новый цех" else "Редактирование цеха")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/workshops" else "/workshops/${form.id}")
    }
}

@Controller
@RequestMapping("/organization-sections")
class OrganizationSectionController(
    private val sectionService: SectionManagementService,
    private val workshopService: WorkshopService,
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", sectionService.listSections(page))
        model.addAttribute("pagePath", "/organization-sections")
        return "organization/section-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, SectionForm(), true)
        return "organization/section-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("section") form: SectionForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "organization/section-form"
        }

        return try {
            sectionService.createSection(form)
            redirectAttributes.addFlashAttribute("successMessage", "Участок добавлен.")
            "redirect:/organization-sections"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("section.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "organization/section-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "section.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить участок. Проверьте уникальность в рамках цеха."),
            )
            populateForm(model, form, true)
            "organization/section-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, sectionService.getSectionForm(id), false)
            "organization/section-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Участок не найден.")
            "redirect:/organization-sections"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("section") form: SectionForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "organization/section-form"
        }

        return try {
            sectionService.updateSection(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Участок обновлен.")
            "redirect:/organization-sections"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("section.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "organization/section-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "section.update",
                dataAccessErrorMessage(ex, "Не удалось обновить участок. Проверьте уникальность в рамках цеха."),
            )
            populateForm(model, form.copy(id = id), false)
            "organization/section-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            sectionService.deleteSection(id)
            redirectAttributes.addFlashAttribute("successMessage", "Участок удален.")
            "redirect:/organization-sections"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить участок, если на него есть ссылки в бригадах, гаражах или ремонтах.")
            "redirect:/organization-sections"
        }

    private fun populateForm(model: Model, form: SectionForm, creating: Boolean) {
        model.addAttribute("section", form)
        model.addAttribute("workshops", workshopService.workshopOptions())
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("pageTitle", if (creating) "Новый участок" else "Редактирование участка")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/organization-sections" else "/organization-sections/${form.id}")
    }
}

@Controller
@RequestMapping("/brigades")
class BrigadeController(
    private val brigadeService: BrigadeService,
    private val sectionService: SectionManagementService,
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", brigadeService.listBrigades(page))
        model.addAttribute("pagePath", "/brigades")
        return "organization/brigade-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, BrigadeForm(), true)
        return "organization/brigade-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("brigade") form: BrigadeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "organization/brigade-form"
        }

        return try {
            brigadeService.createBrigade(form)
            redirectAttributes.addFlashAttribute("successMessage", "Бригада добавлена.")
            "redirect:/brigades"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("brigade.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "organization/brigade-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "brigade.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить бригаду. Проверьте уникальность в рамках участка."),
            )
            populateForm(model, form, true)
            "organization/brigade-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, brigadeService.getBrigadeForm(id), false)
            "organization/brigade-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Бригада не найдена.")
            "redirect:/brigades"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("brigade") form: BrigadeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "organization/brigade-form"
        }

        return try {
            brigadeService.updateBrigade(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Бригада обновлена.")
            "redirect:/brigades"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("brigade.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "organization/brigade-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "brigade.update",
                dataAccessErrorMessage(ex, "Не удалось обновить бригаду. Проверьте уникальность в рамках участка."),
            )
            populateForm(model, form.copy(id = id), false)
            "organization/brigade-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            brigadeService.deleteBrigade(id)
            redirectAttributes.addFlashAttribute("successMessage", "Бригада удалена.")
            "redirect:/brigades"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить бригаду, если на нее есть ссылки в назначениях или ремонтах.")
            "redirect:/brigades"
        }

    private fun populateForm(model: Model, form: BrigadeForm, creating: Boolean) {
        model.addAttribute("brigade", form)
        model.addAttribute("sections", sectionService.sectionOptions())
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("pageTitle", if (creating) "Новая бригада" else "Редактирование бригады")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/brigades" else "/brigades/${form.id}")
    }
}

@Controller
@RequestMapping("/employee-brigade-assignments")
class EmployeeBrigadeAssignmentController(
    private val assignmentService: EmployeeBrigadeAssignmentService,
    private val employeeService: EmployeeService,
    private val brigadeService: BrigadeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", assignmentService.listAssignments(page))
        model.addAttribute("pagePath", "/employee-brigade-assignments")
        return "organization/employee-brigade-assignment-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun createForm(model: Model): String {
        populateForm(model, EmployeeBrigadeAssignmentForm(startDate = LocalDate.now()), true)
        return "organization/employee-brigade-assignment-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun create(
        @Valid @ModelAttribute("assignment") form: EmployeeBrigadeAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "organization/employee-brigade-assignment-form"
        }

        return try {
            assignmentService.createAssignment(form)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение сотрудника в бригаду добавлено.")
            "redirect:/employee-brigade-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("employeeBrigadeAssignment.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "organization/employee-brigade-assignment-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "employeeBrigadeAssignment.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить назначение сотрудника в бригаду."),
            )
            populateForm(model, form, true)
            "organization/employee-brigade-assignment-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, assignmentService.getAssignmentForm(id), false)
            "organization/employee-brigade-assignment-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Назначение сотрудника в бригаду не найдено.")
            "redirect:/employee-brigade-assignments"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("assignment") form: EmployeeBrigadeAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "organization/employee-brigade-assignment-form"
        }

        return try {
            assignmentService.updateAssignment(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение сотрудника в бригаду обновлено.")
            "redirect:/employee-brigade-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("employeeBrigadeAssignment.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "organization/employee-brigade-assignment-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "employeeBrigadeAssignment.update",
                dataAccessErrorMessage(ex, "Не удалось обновить назначение сотрудника в бригаду."),
            )
            populateForm(model, form.copy(id = id), false)
            "organization/employee-brigade-assignment-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).HR)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            assignmentService.deleteAssignment(id)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение сотрудника в бригаду удалено.")
            "redirect:/employee-brigade-assignments"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить назначение сотрудника в бригаду.")
            "redirect:/employee-brigade-assignments"
        }

    private fun populateForm(model: Model, form: EmployeeBrigadeAssignmentForm, creating: Boolean) {
        model.addAttribute("assignment", form)
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("brigades", brigadeService.brigadeOptions())
        model.addAttribute("pageTitle", if (creating) "Новое назначение сотрудника" else "Редактирование назначения сотрудника")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute(
            "formAction",
            if (creating) "/employee-brigade-assignments" else "/employee-brigade-assignments/${form.id}",
        )
    }
}

@Controller
@RequestMapping("/vehicle-driver-assignments")
class VehicleDriverAssignmentController(
    private val assignmentService: VehicleDriverAssignmentService,
    private val vehicleService: VehicleService,
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", assignmentService.listAssignments(page))
        model.addAttribute("pagePath", "/vehicle-driver-assignments")
        return "organization/vehicle-driver-assignment-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun createForm(model: Model): String {
        populateForm(model, VehicleDriverAssignmentForm(startDate = LocalDate.now()), true)
        return "organization/vehicle-driver-assignment-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun create(
        @Valid @ModelAttribute("assignment") form: VehicleDriverAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "organization/vehicle-driver-assignment-form"
        }

        return try {
            assignmentService.createAssignment(form)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение водителя сохранено.")
            "redirect:/vehicle-driver-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleDriverAssignment.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "organization/vehicle-driver-assignment-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "vehicleDriverAssignment.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить назначение водителя."),
            )
            populateForm(model, form, true)
            "organization/vehicle-driver-assignment-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, assignmentService.getAssignmentForm(id), false)
            "organization/vehicle-driver-assignment-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Назначение водителя не найдено.")
            "redirect:/vehicle-driver-assignments"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("assignment") form: VehicleDriverAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "organization/vehicle-driver-assignment-form"
        }

        return try {
            assignmentService.updateAssignment(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение водителя обновлено.")
            "redirect:/vehicle-driver-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleDriverAssignment.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "organization/vehicle-driver-assignment-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "vehicleDriverAssignment.update",
                dataAccessErrorMessage(ex, "Не удалось обновить назначение водителя."),
            )
            populateForm(model, form.copy(id = id), false)
            "organization/vehicle-driver-assignment-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            assignmentService.deleteAssignment(id)
            redirectAttributes.addFlashAttribute("successMessage", "Назначение водителя удалено.")
            "redirect:/vehicle-driver-assignments"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить назначение водителя.")
            "redirect:/vehicle-driver-assignments"
        }

    private fun populateForm(model: Model, form: VehicleDriverAssignmentForm, creating: Boolean) {
        model.addAttribute("assignment", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("employees", employeeService.driverOptions())
        model.addAttribute("assignmentTypes", VehicleDriverAssignmentService.assignmentTypes)
        model.addAttribute("pageTitle", if (creating) "Новое назначение водителя" else "Редактирование назначения водителя")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute(
            "formAction",
            if (creating) "/vehicle-driver-assignments" else "/vehicle-driver-assignments/${form.id}",
        )
    }
}
