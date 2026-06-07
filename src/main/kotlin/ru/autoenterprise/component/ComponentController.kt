package ru.autoenterprise.component

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
import ru.autoenterprise.repair.RepairService
import ru.autoenterprise.vehicle.VehicleService

@Controller
class ComponentsController {

    @GetMapping("/components")
    fun index(): String = "component/index"
}

@Controller
@RequestMapping("/components-catalog")
class ComponentCatalogController(
    private val componentService: ComponentService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", componentService.listComponents(page))
        model.addAttribute("pagePath", "/components-catalog")
        return "component/component-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun createForm(model: Model): String {
        populateForm(model, ComponentForm(), true)
        return "component/component-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun create(
        @Valid @ModelAttribute("component") form: ComponentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "component/component-form"
        }

        return try {
            componentService.createComponent(form)
            redirectAttributes.addFlashAttribute("successMessage", "Агрегат добавлен.")
            "redirect:/components-catalog"
        } catch (_: DataAccessException) {
            bindingResult.reject("component.save", "Не удалось сохранить агрегат. Серийный номер должен быть уникальным.")
            populateForm(model, form, true)
            "component/component-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, componentService.getComponentForm(id), false)
            "component/component-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Агрегат не найден.")
            "redirect:/components-catalog"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("component") form: ComponentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "component/component-form"
        }

        return try {
            componentService.updateComponent(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Агрегат обновлен.")
            "redirect:/components-catalog"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("component.update", "Агрегат не найден.")
            populateForm(model, form.copy(id = id), false)
            "component/component-form"
        } catch (_: DataAccessException) {
            bindingResult.reject("component.update", "Не удалось обновить агрегат. Серийный номер должен быть уникальным.")
            populateForm(model, form.copy(id = id), false)
            "component/component-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            componentService.deleteComponent(id)
            redirectAttributes.addFlashAttribute("successMessage", "Агрегат удален.")
            "redirect:/components-catalog"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить агрегат, если на него ссылается история установки или ремонта.")
            "redirect:/components-catalog"
        }

    private fun populateForm(model: Model, form: ComponentForm, creating: Boolean) {
        model.addAttribute("component", form)
        model.addAttribute("pageTitle", if (creating) "Новый агрегат" else "Редактирование агрегата")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/components-catalog" else "/components-catalog/${form.id}")
    }
}

@Controller
@RequestMapping("/component-history")
class VehicleComponentHistoryController(
    private val vehicleComponentHistoryService: VehicleComponentHistoryService,
    private val vehicleService: VehicleService,
    private val componentService: ComponentService,
    private val repairService: RepairService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", vehicleComponentHistoryService.listHistory(page))
        model.addAttribute("pagePath", "/component-history")
        return "component/history-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun createForm(model: Model): String {
        populateForm(model, VehicleComponentHistoryForm(actionDate = LocalDate.now()), true)
        return "component/history-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun create(
        @Valid @ModelAttribute("history") form: VehicleComponentHistoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "component/history-form"
        }

        return try {
            vehicleComponentHistoryService.createHistory(form)
            redirectAttributes.addFlashAttribute("successMessage", "История агрегата добавлена.")
            "redirect:/component-history"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("componentHistory.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "component/history-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject("componentHistory.save", componentHistoryErrorMessage(ex))
            populateForm(model, form, true)
            "component/history-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, vehicleComponentHistoryService.getHistoryForm(id), false)
            "component/history-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "История агрегата не найдена.")
            "redirect:/component-history"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("history") form: VehicleComponentHistoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "component/history-form"
        }

        return try {
            vehicleComponentHistoryService.updateHistory(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "История агрегата обновлена.")
            "redirect:/component-history"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("componentHistory.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "component/history-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject("componentHistory.update", componentHistoryErrorMessage(ex))
            populateForm(model, form.copy(id = id), false)
            "component/history-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            vehicleComponentHistoryService.deleteHistory(id)
            redirectAttributes.addFlashAttribute("successMessage", "История агрегата удалена.")
            "redirect:/component-history"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить историю агрегата.")
            "redirect:/component-history"
        }

    private fun populateForm(model: Model, form: VehicleComponentHistoryForm, creating: Boolean) {
        model.addAttribute("history", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("components", componentService.componentOptions())
        model.addAttribute("repairs", repairService.repairOptions())
        model.addAttribute("actionTypes", VehicleComponentHistoryService.actionTypes)
        model.addAttribute("pageTitle", if (creating) "Новая запись истории агрегата" else "Редактирование истории агрегата")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/component-history" else "/component-history/${form.id}")
    }

    private fun componentHistoryErrorMessage(exception: DataAccessException): String {
        val details = generateSequence<Throwable>(exception) { cause -> cause.cause }
            .mapNotNull { cause -> cause.message }
            .joinToString("\n")

        return when {
            "уже установлен" in details -> "Агрегат уже установлен. Сначала добавьте запись о снятии или замене."
            "не был установлен" in details || "так как он не установлен" in details ->
                "Снять, заменить или отремонтировать можно только ранее установленный агрегат."
            "установлен на другом автомобиле" in details ->
                "Выбранный агрегат установлен на другом автомобиле."
            "ремонт относится к другому автомобилю" in details ->
                "Выбранный ремонт относится к другому автомобилю."
            "Дата действия должна входить" in details ->
                "Дата действия должна входить в период выбранного ремонта."
            "необходимо выбрать ремонт" in details ->
                "Для ремонта или замены агрегата необходимо выбрать ремонт."
            "повторная установка" in details || "следующим действием может быть только установка" in details ->
                "Действие нарушает хронологическую последовательность истории агрегата."
            else -> "Не удалось сохранить историю агрегата. Проверьте последовательность действий, транспорт, ремонт и дату."
        }
    }
}
