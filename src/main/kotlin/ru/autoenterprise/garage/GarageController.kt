package ru.autoenterprise.garage

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
import ru.autoenterprise.organization.SectionManagementService
import ru.autoenterprise.organization.WorkshopService
import ru.autoenterprise.vehicle.VehicleService
import ru.autoenterprise.web.dataAccessErrorMessage

@Controller
class GarageController {

    @GetMapping("/garage")
    fun index(): String = "garage/index"
}

@Controller
@RequestMapping("/garage-objects")
class GarageObjectController(
    private val garageObjectService: GarageObjectService,
    private val workshopService: WorkshopService,
    private val sectionService: SectionManagementService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", garageObjectService.listGarageObjects(page))
        model.addAttribute("pagePath", "/garage-objects")
        return "garage/object-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createForm(model: Model): String {
        populateForm(model, GarageObjectForm(), true)
        return "garage/object-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun create(
        @Valid @ModelAttribute("garageObject") form: GarageObjectForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "garage/object-form"
        }

        return try {
            garageObjectService.createGarageObject(form)
            redirectAttributes.addFlashAttribute("successMessage", "Объект гаражного хозяйства добавлен.")
            "redirect:/garage-objects"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("garageObject.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "garage/object-form"
        } catch (ex: IllegalStateException) {
            bindingResult.reject("garageObject.save", ex.message ?: "Проверьте данные формы.")
            populateForm(model, form, true)
            "garage/object-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "garageObject.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить объект гаражного хозяйства."),
            )
            populateForm(model, form, true)
            "garage/object-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, garageObjectService.getGarageObjectForm(id), false)
            "garage/object-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Объект гаражного хозяйства не найден.")
            "redirect:/garage-objects"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("garageObject") form: GarageObjectForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "garage/object-form"
        }

        return try {
            garageObjectService.updateGarageObject(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Объект гаражного хозяйства обновлен.")
            "redirect:/garage-objects"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("garageObject.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "garage/object-form"
        } catch (ex: IllegalStateException) {
            bindingResult.reject("garageObject.update", ex.message ?: "Проверьте данные формы.")
            populateForm(model, form.copy(id = id), false)
            "garage/object-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "garageObject.update",
                dataAccessErrorMessage(ex, "Не удалось обновить объект гаражного хозяйства."),
            )
            populateForm(model, form.copy(id = id), false)
            "garage/object-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            garageObjectService.deleteGarageObject(id)
            redirectAttributes.addFlashAttribute("successMessage", "Объект гаражного хозяйства удален.")
            "redirect:/garage-objects"
        } catch (ex: IllegalStateException) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.message ?: "Нельзя удалить объект гаражного хозяйства.")
            "redirect:/garage-objects"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить объект, если на него ссылаются дочерние объекты или история размещения.")
            "redirect:/garage-objects"
        }

    private fun populateForm(model: Model, form: GarageObjectForm, creating: Boolean) {
        model.addAttribute("garageObject", form)
        model.addAttribute("objectTypes", GarageObjectService.objectTypes)
        model.addAttribute("parents", garageObjectService.garageObjectOptions(form.id))
        model.addAttribute("workshops", workshopService.workshopOptions())
        model.addAttribute("sections", sectionService.sectionOptions())
        model.addAttribute("pageTitle", if (creating) "Новый объект гаражного хозяйства" else "Редактирование объекта гаражного хозяйства")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/garage-objects" else "/garage-objects/${form.id}")
    }
}

@Controller
@RequestMapping("/vehicle-locations")
class VehicleLocationHistoryController(
    private val vehicleLocationHistoryService: VehicleLocationHistoryService,
    private val vehicleService: VehicleService,
    private val garageObjectService: GarageObjectService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", vehicleLocationHistoryService.listLocations(page))
        model.addAttribute("pagePath", "/vehicle-locations")
        return "garage/location-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun createForm(model: Model): String {
        populateForm(model, VehicleLocationHistoryForm(startDate = LocalDate.now()), true)
        return "garage/location-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun create(
        @Valid @ModelAttribute("location") form: VehicleLocationHistoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "garage/location-form"
        }

        return try {
            vehicleLocationHistoryService.createLocation(form)
            redirectAttributes.addFlashAttribute("successMessage", "История размещения транспорта добавлена.")
            "redirect:/vehicle-locations"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleLocation.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "garage/location-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "vehicleLocation.save",
                dataAccessErrorMessage(ex, "Не удалось сохранить историю размещения транспорта."),
            )
            populateForm(model, form, true)
            "garage/location-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, vehicleLocationHistoryService.getLocationForm(id), false)
            "garage/location-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "История размещения транспорта не найдена.")
            "redirect:/vehicle-locations"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("location") form: VehicleLocationHistoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "garage/location-form"
        }

        return try {
            vehicleLocationHistoryService.updateLocation(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "История размещения транспорта обновлена.")
            "redirect:/vehicle-locations"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicleLocation.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "garage/location-form"
        } catch (ex: DataAccessException) {
            bindingResult.reject(
                "vehicleLocation.update",
                dataAccessErrorMessage(ex, "Не удалось обновить историю размещения транспорта."),
            )
            populateForm(model, form.copy(id = id), false)
            "garage/location-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            vehicleLocationHistoryService.deleteLocation(id)
            redirectAttributes.addFlashAttribute("successMessage", "История размещения транспорта удалена.")
            "redirect:/vehicle-locations"
        } catch (_: DataAccessException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить историю размещения транспорта.")
            "redirect:/vehicle-locations"
        }

    private fun populateForm(model: Model, form: VehicleLocationHistoryForm, creating: Boolean) {
        model.addAttribute("location", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("garageObjects", garageObjectService.garageObjectOptions())
        model.addAttribute("pageTitle", if (creating) "Новое размещение транспорта" else "Редактирование размещения транспорта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/vehicle-locations" else "/vehicle-locations/${form.id}")
    }
}
