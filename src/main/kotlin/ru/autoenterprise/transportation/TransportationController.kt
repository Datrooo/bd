package ru.autoenterprise.transportation

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
import ru.autoenterprise.route.RouteService
import ru.autoenterprise.vehicle.VehicleService

@Controller
class TransportationController {

    @GetMapping("/transportation")
    fun index(): String = "transportation/index"
}

@Controller
@RequestMapping("/route-vehicle-assignments")
class RouteVehicleAssignmentController(
    private val routeVehicleAssignmentService: RouteVehicleAssignmentService,
    private val routeService: RouteService,
    private val vehicleService: VehicleService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", routeVehicleAssignmentService.listAssignments(page))
        model.addAttribute("pagePath", "/route-vehicle-assignments")
        return "transportation/route-assignment-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun createForm(model: Model): String {
        populateForm(model, RouteVehicleAssignmentForm(startDate = LocalDate.now()), true)
        return "transportation/route-assignment-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun create(
        @Valid @ModelAttribute("assignment") form: RouteVehicleAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "transportation/route-assignment-form"
        }

        return try {
            routeVehicleAssignmentService.createAssignment(form)
            redirectAttributes.addFlashAttribute("successMessage", "Закрепление транспорта за маршрутом добавлено.")
            "redirect:/route-vehicle-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("routeVehicleAssignment.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "transportation/route-assignment-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("routeVehicleAssignment.save", "Не удалось сохранить закрепление транспорта за маршрутом.")
            populateForm(model, form, true)
            "transportation/route-assignment-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, routeVehicleAssignmentService.getAssignmentForm(id), false)
            "transportation/route-assignment-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Закрепление транспорта за маршрутом не найдено.")
            "redirect:/route-vehicle-assignments"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("assignment") form: RouteVehicleAssignmentForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "transportation/route-assignment-form"
        }

        return try {
            routeVehicleAssignmentService.updateAssignment(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Закрепление транспорта за маршрутом обновлено.")
            "redirect:/route-vehicle-assignments"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("routeVehicleAssignment.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "transportation/route-assignment-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("routeVehicleAssignment.update", "Не удалось обновить закрепление транспорта за маршрутом.")
            populateForm(model, form.copy(id = id), false)
            "transportation/route-assignment-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            routeVehicleAssignmentService.deleteAssignment(id)
            redirectAttributes.addFlashAttribute("successMessage", "Закрепление транспорта за маршрутом удалено.")
            "redirect:/route-vehicle-assignments"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить закрепление транспорта за маршрутом.")
            "redirect:/route-vehicle-assignments"
        }

    private fun populateForm(model: Model, form: RouteVehicleAssignmentForm, creating: Boolean) {
        model.addAttribute("assignment", form)
        model.addAttribute("routes", routeService.routeOptions())
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("pageTitle", if (creating) "Новое закрепление транспорта" else "Редактирование закрепления транспорта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/route-vehicle-assignments" else "/route-vehicle-assignments/${form.id}")
    }
}

@Controller
@RequestMapping("/transportation-records")
class TransportationRecordController(
    private val transportationRecordService: TransportationRecordService,
    private val routeService: RouteService,
    private val vehicleService: VehicleService,
) {

    @GetMapping
    fun list(@RequestParam(name = "page", defaultValue = "0") page: Int, model: Model): String {
        model.addAttribute("page", transportationRecordService.listRecords(page))
        model.addAttribute("pagePath", "/transportation-records")
        return "transportation/record-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun createForm(model: Model): String {
        populateForm(model, TransportationRecordForm(recordDate = LocalDate.now()), true)
        return "transportation/record-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun create(
        @Valid @ModelAttribute("record") form: TransportationRecordForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "transportation/record-form"
        }

        return try {
            transportationRecordService.createRecord(form)
            redirectAttributes.addFlashAttribute("successMessage", "Эксплуатационная запись добавлена.")
            "redirect:/transportation-records"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("transportationRecord.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "transportation/record-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("transportationRecord.save", "Не удалось сохранить эксплуатационную запись. Проверьте тип записи и заполненные поля.")
            populateForm(model, form, true)
            "transportation/record-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun edit(@PathVariable id: Long, model: Model, redirectAttributes: RedirectAttributes): String =
        try {
            populateForm(model, transportationRecordService.getRecordForm(id), false)
            "transportation/record-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Эксплуатационная запись не найдена.")
            "redirect:/transportation-records"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute("record") form: TransportationRecordForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "transportation/record-form"
        }

        return try {
            transportationRecordService.updateRecord(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Эксплуатационная запись обновлена.")
            "redirect:/transportation-records"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("transportationRecord.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form.copy(id = id), false)
            "transportation/record-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("transportationRecord.update", "Не удалось обновить эксплуатационную запись. Проверьте тип записи и заполненные поля.")
            populateForm(model, form.copy(id = id), false)
            "transportation/record-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String =
        try {
            transportationRecordService.deleteRecord(id)
            redirectAttributes.addFlashAttribute("successMessage", "Эксплуатационная запись удалена.")
            "redirect:/transportation-records"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить эксплуатационную запись.")
            "redirect:/transportation-records"
        }

    private fun populateForm(model: Model, form: TransportationRecordForm, creating: Boolean) {
        model.addAttribute("record", form)
        model.addAttribute("vehicles", vehicleService.vehicleOptions())
        model.addAttribute("routes", routeService.routeOptions())
        model.addAttribute("recordTypes", TransportationRecordService.recordTypes)
        model.addAttribute("pageTitle", if (creating) "Новая эксплуатационная запись" else "Редактирование эксплуатационной записи")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/transportation-records" else "/transportation-records/${form.id}")
    }
}
