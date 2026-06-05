package ru.autoenterprise.vehicle

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class VehicleController(
    private val vehicleService: VehicleService,
    private val categoryService: VehicleCategoryService,
) {

    @GetMapping("/vehicles")
    fun listVehicles(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("page", vehicleService.listVehicles(page))
        model.addAttribute("pagePath", "/vehicles")
        return "vehicle/list"
    }

    @GetMapping("/vehicles/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun newVehicle(model: Model): String {
        populateVehicleForm(model, VehicleForm(), true)
        return "vehicle/form"
    }

    @PostMapping("/vehicles")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createVehicle(
        @Valid @ModelAttribute("vehicle") form: VehicleForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateVehicleForm(model, form, true)
            return "vehicle/form"
        }

        return try {
            vehicleService.createVehicle(form)
            redirectAttributes.addFlashAttribute("successMessage", "Транспорт добавлен.")
            "redirect:/vehicles"
        } catch (ex: EntityNotFoundException) {
            bindingResult.rejectValue("categoryId", "vehicle.categoryId", ex.message ?: "Категория транспорта не найдена.")
            populateVehicleForm(model, form, true)
            "vehicle/form"
        } catch (ex: DataIntegrityViolationException) {
            bindingResult.reject("vehicle.save", "Не удалось сохранить транспорт. Проверьте уникальность номеров и обязательные поля.")
            populateVehicleForm(model, form, true)
            "vehicle/form"
        }
    }

    @GetMapping("/vehicles/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun editVehicle(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            populateVehicleForm(model, vehicleService.getVehicleForm(id), false)
            "vehicle/form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Транспорт не найден.")
            "redirect:/vehicles"
        }

    @PostMapping("/vehicles/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN, T(ru.autoenterprise.security.AppRole).DISPATCHER, T(ru.autoenterprise.security.AppRole).MECHANIC)")
    fun updateVehicle(
        @PathVariable id: Long,
        @Valid @ModelAttribute("vehicle") form: VehicleForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateVehicleForm(model, form.copy(id = id), false)
            return "vehicle/form"
        }

        return try {
            vehicleService.updateVehicle(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Транспорт обновлен.")
            "redirect:/vehicles"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("vehicle.update", ex.message ?: "Транспорт не найден.")
            populateVehicleForm(model, form.copy(id = id), false)
            "vehicle/form"
        } catch (ex: DataIntegrityViolationException) {
            bindingResult.reject("vehicle.update", "Не удалось обновить транспорт. Проверьте уникальность номеров и обязательные поля.")
            populateVehicleForm(model, form.copy(id = id), false)
            "vehicle/form"
        }
    }

    @DeleteMapping("/vehicles/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun deleteVehicle(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            vehicleService.deleteVehicle(id)
            redirectAttributes.addFlashAttribute("successMessage", "Транспорт удален.")
            "redirect:/vehicles"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить транспорт, на который уже ссылаются связанные записи.")
            "redirect:/vehicles"
        }

    private fun populateVehicleForm(model: Model, form: VehicleForm, creating: Boolean) {
        model.addAttribute("vehicle", form)
        model.addAttribute("categories", categoryService.categoryOptions())
        model.addAttribute("statuses", VehicleService.vehicleStatuses)
        model.addAttribute("pageTitle", if (creating) "Новый транспорт" else "Редактирование транспорта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/vehicles" else "/vehicles/${form.id}")
    }
}

@Controller
@RequestMapping("/vehicle-categories")
class VehicleCategoryController(
    private val categoryService: VehicleCategoryService,
) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun listCategories(model: Model): String {
        model.addAttribute("categories", categoryService.listCategories())
        return "vehicle/category-list"
    }

    @GetMapping("/new")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun newCategory(model: Model): String {
        populateCategoryForm(model, VehicleCategoryForm(), true)
        return "vehicle/category-form"
    }

    @PostMapping
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun createCategory(
        @Valid @ModelAttribute("category") form: VehicleCategoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateCategoryForm(model, form, true)
            return "vehicle/category-form"
        }

        return try {
            categoryService.createCategory(form)
            redirectAttributes.addFlashAttribute("successMessage", "Категория транспорта добавлена.")
            "redirect:/vehicle-categories"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("category.save", "Не удалось сохранить категорию. Название должно быть уникальным.")
            populateCategoryForm(model, form, true)
            "vehicle/category-form"
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun editCategory(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            populateCategoryForm(model, categoryService.getCategoryForm(id), false)
            "vehicle/category-form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Категория транспорта не найдена.")
            "redirect:/vehicle-categories"
        }

    @PostMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @ModelAttribute("category") form: VehicleCategoryForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            populateCategoryForm(model, form.copy(id = id), false)
            return "vehicle/category-form"
        }

        return try {
            categoryService.updateCategory(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Категория транспорта обновлена.")
            "redirect:/vehicle-categories"
        } catch (_: EntityNotFoundException) {
            bindingResult.reject("category.update", "Категория транспорта не найдена.")
            populateCategoryForm(model, form.copy(id = id), false)
            "vehicle/category-form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("category.update", "Не удалось обновить категорию. Название должно быть уникальным.")
            populateCategoryForm(model, form.copy(id = id), false)
            "vehicle/category-form"
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun deleteCategory(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            categoryService.deleteCategory(id)
            redirectAttributes.addFlashAttribute("successMessage", "Категория транспорта удалена.")
            "redirect:/vehicle-categories"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить категорию, если на нее ссылаются транспортные средства.")
            "redirect:/vehicle-categories"
        }

    private fun populateCategoryForm(model: Model, form: VehicleCategoryForm, creating: Boolean) {
        model.addAttribute("category", form)
        model.addAttribute("pageTitle", if (creating) "Новая категория транспорта" else "Редактирование категории транспорта")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/vehicle-categories" else "/vehicle-categories/${form.id}")
    }
}
