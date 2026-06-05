package ru.autoenterprise.vehicle

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import java.time.LocalDate
import org.springframework.dao.DataIntegrityViolationException
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

@Controller
@RequestMapping("/vehicle-acquisitions")
class VehicleAcquisitionController(
    private val vehicleAcquisitionService: VehicleAcquisitionService,
    private val vehicleService: VehicleService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", vehicleAcquisitionService.listAcquisitions(page))
        model.addAttribute("pagePath", "/vehicle-acquisitions")
        return "vehicle/acquisition-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, VehicleAcquisitionForm(acquisitionDate = LocalDate.now()), true)
        return "vehicle/acquisition-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("acquisition") form: VehicleAcquisitionForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "vehicle/acquisition-form"
        }

        return try {
            vehicleAcquisitionService.createAcquisition(form)
            redirectAttributes.addFlashAttribute("successMessage", "Документ поступления добавлен.")
            "redirect:/vehicle-acquisitions"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleAcquisition.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "vehicle/acquisition-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("vehicleAcquisition.save", "Не удалось сохранить документ поступления.")
            populateForm(model, form, true)
            "vehicle/acquisition-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, vehicleAcquisitionService.getAcquisitionForm(id), false)
            "vehicle/acquisition-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Документ поступления не найден.")
            "redirect:/vehicle-acquisitions"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("acquisition") form: VehicleAcquisitionForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "vehicle/acquisition-form"
        }

        return try {
            vehicleAcquisitionService.updateAcquisition(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Документ поступления обновлен.")
            "redirect:/vehicle-acquisitions"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleAcquisition.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "vehicle/acquisition-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("vehicleAcquisition.update", "Не удалось обновить документ поступления.")
            populateForm(model, form.copy(id = id), false)
            "vehicle/acquisition-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            vehicleAcquisitionService.deleteAcquisition(id)
            redirectAttributes.addFlashAttribute("successMessage", "Документ поступления удален.")
            "redirect:/vehicle-acquisitions"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить документ поступления.")
            "redirect:/vehicle-acquisitions"
        }

    private fun populateForm(model: Model, form: VehicleAcquisitionForm, creating: Boolean) {
        model.addAttribute("acquisition", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("acquisitionTypes", VehicleAcquisitionService.acquisitionTypes)
        model.addAttribute("pageTitle", if (creating) "Новый документ поступления" else "Редактирование документа поступления")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/vehicle-acquisitions" else "/vehicle-acquisitions/${form.id}")
    }
}

@Controller
@RequestMapping("/vehicle-disposals")
class VehicleDisposalController(
    private val vehicleDisposalService: VehicleDisposalService,
    private val vehicleService: VehicleService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", vehicleDisposalService.listDisposals(page))
        model.addAttribute("pagePath", "/vehicle-disposals")
        return "vehicle/disposal-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, VehicleDisposalForm(disposalDate = LocalDate.now()), true)
        return "vehicle/disposal-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("disposal") form: VehicleDisposalForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "vehicle/disposal-form"
        }

        return try {
            vehicleDisposalService.createDisposal(form)
            redirectAttributes.addFlashAttribute("successMessage", "Документ выбытия добавлен.")
            "redirect:/vehicle-disposals"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleDisposal.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "vehicle/disposal-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("vehicleDisposal.save", "Не удалось сохранить документ выбытия.")
            populateForm(model, form, true)
            "vehicle/disposal-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, vehicleDisposalService.getDisposalForm(id), false)
            "vehicle/disposal-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Документ выбытия не найден.")
            "redirect:/vehicle-disposals"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("disposal") form: VehicleDisposalForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "vehicle/disposal-form"
        }

        return try {
            vehicleDisposalService.updateDisposal(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Документ выбытия обновлен.")
            "redirect:/vehicle-disposals"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleDisposal.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "vehicle/disposal-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("vehicleDisposal.update", "Не удалось обновить документ выбытия.")
            populateForm(model, form.copy(id = id), false)
            "vehicle/disposal-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            vehicleDisposalService.deleteDisposal(id)
            redirectAttributes.addFlashAttribute("successMessage", "Документ выбытия удален.")
            "redirect:/vehicle-disposals"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить документ выбытия.")
            "redirect:/vehicle-disposals"
        }

    private fun populateForm(model: Model, form: VehicleDisposalForm, creating: Boolean) {
        model.addAttribute("disposal", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("disposalTypes", VehicleDisposalService.disposalTypes)
        model.addAttribute("pageTitle", if (creating) "Новый документ выбытия" else "Редактирование документа выбытия")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/vehicle-disposals" else "/vehicle-disposals/${form.id}")
    }
}
