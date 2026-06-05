package ru.autoenterprise.route

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class RouteController(
    private val routeService: RouteService,
) {

    @GetMapping("/routes")
    fun listRoutes(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("page", routeService.listRoutes(page))
        model.addAttribute("pagePath", "/routes")
        return "route/list"
    }

    @GetMapping("/routes/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun newRoute(model: Model): String {
        populateForm(model, RouteForm(), true)
        return "route/form"
    }

    @PostMapping("/routes")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun createRoute(
        @Valid @ModelAttribute("route") form: RouteForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "route/form"
        }

        return try {
            routeService.createRoute(form)
            redirectAttributes.addFlashAttribute("successMessage", "Маршрут добавлен.")
            "redirect:/routes"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("route.save", "Не удалось сохранить маршрут. Проверьте уникальность номера/типа и значения полей.")
            populateForm(model, form, true)
            "route/form"
        }
    }

    @GetMapping("/routes/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun editRoute(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            populateForm(model, routeService.getRouteForm(id), false)
            "route/form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Маршрут не найден.")
            "redirect:/routes"
        }

    @PostMapping("/routes/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun updateRoute(
        @PathVariable id: Long,
        @Valid @ModelAttribute("route") form: RouteForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateForm(model, form.copy(id = id), false)
            return "route/form"
        }

        return try {
            routeService.updateRoute(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Маршрут обновлен.")
            "redirect:/routes"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("route.update", "Маршрут не найден.")
            populateForm(model, form.copy(id = id), false)
            "route/form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("route.update", "Не удалось обновить маршрут. Проверьте уникальность номера/типа и значения полей.")
            populateForm(model, form.copy(id = id), false)
            "route/form"
        }
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER)")
    fun deleteRoute(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            routeService.deleteRoute(id)
            redirectAttributes.addFlashAttribute("successMessage", "Маршрут удален.")
            "redirect:/routes"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить маршрут, если на него ссылаются назначения или записи эксплуатации.")
            "redirect:/routes"
        }

    private fun populateForm(model: Model, form: RouteForm, creating: Boolean) {
        model.addAttribute("route", form)
        model.addAttribute("routeTypes", RouteService.routeTypes)
        model.addAttribute("pageTitle", if (creating) "Новый маршрут" else "Редактирование маршрута")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/routes" else "/routes/${form.id}")
    }
}
