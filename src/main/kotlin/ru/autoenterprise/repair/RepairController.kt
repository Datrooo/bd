package ru.autoenterprise.repair

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
import ru.autoenterprise.organization.BrigadeService
import ru.autoenterprise.organization.SectionManagementService
import ru.autoenterprise.organization.WorkshopService
import ru.autoenterprise.vehicle.VehicleService

@Controller
class RepairsController {

    @GetMapping("/repairs")
    fun index(): String = "repair/index"
}

@Controller
@RequestMapping("/repair-types")
class RepairTypeController(
    private val repairTypeService: RepairTypeService,
) {

    @GetMapping
    fun list(model: Model): String {
        model.addAttribute("types", repairTypeService.listTypes())
        return "repair/type-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, RepairTypeForm(), true)
        return "repair/type-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("repairType") form: RepairTypeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "repair/type-form"
        }

        return try {
            repairTypeService.createType(form)
            redirectAttributes.addFlashAttribute("successMessage", "Тип ремонта добавлен.")
            "redirect:/repair-types"
        } catch (_: DataAccessException) {
            bindingResult.reject("repairType.save", "Не удалось сохранить тип ремонта. Название должно быть уникальным.")
            populateForm(model, form, true)
            "repair/type-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, repairTypeService.getTypeForm(id), false)
            "repair/type-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Тип ремонта не найден.")
            "redirect:/repair-types"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("repairType") form: RepairTypeForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "repair/type-form"
        }

        return try {
            repairTypeService.updateType(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Тип ремонта обновлен.")
            "redirect:/repair-types"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("repairType.update", "Тип ремонта не найден.")
            populateForm(model, form.copy(id = id), false)
            "repair/type-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("repairType.update", "Не удалось обновить тип ремонта. Название должно быть уникальным.")
            populateForm(model, form.copy(id = id), false)
            "repair/type-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            repairTypeService.deleteType(id)
            redirectAttributes.addFlashAttribute("successMessage", "Тип ремонта удален.")
            "redirect:/repair-types"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить тип ремонта, если на него ссылаются ремонты.")
            "redirect:/repair-types"
        }

    private fun populateForm(model: Model, form: RepairTypeForm, creating: Boolean) {
        model.addAttribute("repairType", form)
        model.addAttribute("pageTitle", if (creating) "Новый тип ремонта" else "Редактирование типа ремонта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/repair-types" else "/repair-types/${form.id}")
    }
}

@Controller
@RequestMapping("/repairs-journal")
class RepairJournalController(
    private val repairService: RepairService,
    private val vehicleService: VehicleService,
    private val repairTypeService: RepairTypeService,
    private val workshopService: WorkshopService,
    private val sectionService: SectionManagementService,
    private val brigadeService: BrigadeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", repairService.listRepairs(page))
        model.addAttribute("pagePath", "/repairs-journal")
        return "repair/repair-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun createForm(model: Model): String {
        populateForm(model, RepairForm(startDate = LocalDate.now()), true)
        return "repair/repair-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun create(
        @Valid @ModelAttribute("repair") form: RepairForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "repair/repair-form"
        }

        return try {
            repairService.createRepair(form)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонт добавлен.")
            "redirect:/repairs-journal"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("repair.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "repair/repair-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("repair.save", "Не удалось сохранить ремонт.")
            populateForm(model, form, true)
            "repair/repair-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, repairService.getRepairForm(id), false)
            "repair/repair-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ремонт не найден.")
            "redirect:/repairs-journal"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("repair") form: RepairForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "repair/repair-form"
        }

        return try {
            repairService.updateRepair(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонт обновлен.")
            "redirect:/repairs-journal"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("repair.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "repair/repair-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("repair.update", "Не удалось обновить ремонт.")
            populateForm(model, form.copy(id = id), false)
            "repair/repair-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            repairService.deleteRepair(id)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонт удален.")
            "redirect:/repairs-journal"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить ремонт, если на него ссылаются работы или история агрегатов.")
            "redirect:/repairs-journal"
        }

    private fun populateForm(model: Model, form: RepairForm, creating: Boolean) {
        model.addAttribute("repair", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("repairTypes", repairTypeService.typeOptions())
        model.addAttribute("workshops", workshopService.workshopOptions())
        model.addAttribute("sections", sectionService.sectionOptions())
        model.addAttribute("brigades", brigadeService.brigadeOptions())
        model.addAttribute("repairStatuses", RepairService.repairStatuses)
        model.addAttribute("pageTitle", if (creating) "Новый ремонт" else "Редактирование ремонта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/repairs-journal" else "/repairs-journal/${form.id}")
    }
}

@Controller
@RequestMapping("/repair-works")
class RepairWorkController(
    private val repairWorkService: RepairWorkService,
    private val repairService: RepairService,
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", repairWorkService.listWorks(page))
        model.addAttribute("pagePath", "/repair-works")
        return "repair/work-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun createForm(model: Model): String {
        populateForm(model, RepairWorkForm(), true)
        return "repair/work-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun create(
        @Valid @ModelAttribute("repairWork") form: RepairWorkForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "repair/work-form"
        }

        return try {
            repairWorkService.createWork(form)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонтная работа добавлена.")
            "redirect:/repair-works"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("repairWork.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "repair/work-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("repairWork.save", "Не удалось сохранить ремонтную работу.")
            populateForm(model, form, true)
            "repair/work-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, repairWorkService.getWorkForm(id), false)
            "repair/work-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ремонтная работа не найдена.")
            "redirect:/repair-works"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("repairWork") form: RepairWorkForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "repair/work-form"
        }

        return try {
            repairWorkService.updateWork(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонтная работа обновлена.")
            "redirect:/repair-works"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("repairWork.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "repair/work-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("repairWork.update", "Не удалось обновить ремонтную работу.")
            populateForm(model, form.copy(id = id), false)
            "repair/work-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            repairWorkService.deleteWork(id)
            redirectAttributes.addFlashAttribute("successMessage", "Ремонтная работа удалена.")
            "redirect:/repair-works"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить ремонтную работу.")
            "redirect:/repair-works"
        }

    private fun populateForm(model: Model, form: RepairWorkForm, creating: Boolean) {
        model.addAttribute("repairWork", form)
        model.addAttribute("repairs", repairService.repairOptions())
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("pageTitle", if (creating) "Новая ремонтная работа" else "Редактирование ремонтной работы")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/repair-works" else "/repair-works/${form.id}")
    }
}
