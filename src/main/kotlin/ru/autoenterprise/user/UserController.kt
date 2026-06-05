package ru.autoenterprise.user

import ru.autoenterprise.employee.EmployeeService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
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
class UserController(
    private val userService: UserService,
    private val employeeService: EmployeeService,
) {

    @GetMapping("/users")
    @PreAuthorize("@roleAccess.hasAny(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN, T(ru.autoenterprise.security.AppRole).ADMIN)")
    fun listUsers(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("page", userService.listUsers(page))
        model.addAttribute("pagePath", "/users")
        return "user/list"
    }

    @GetMapping("/users/new")
    @PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
    fun newUser(model: Model): String {
        populateForm(model, UserForm(active = true), true)
        return "user/form"
    }

    @PostMapping("/users")
    @PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
    fun createUser(
        @Valid @ModelAttribute("user") form: UserForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        validateUserForm(form, bindingResult, true)
        if (bindingResult.hasErrors()) {
            populateForm(model, form, true)
            return "user/form"
        }

        return try {
            userService.createUser(form)
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь создан.")
            "redirect:/users"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("user.save", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, true)
            "user/form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("user.save", "Не удалось сохранить пользователя. Логин должен быть уникальным.")
            populateForm(model, form, true)
            "user/form"
        }
    }

    @GetMapping("/users/{id}/edit")
    @PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
    fun editUser(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            populateForm(model, userService.getUserForm(id), false)
            "user/form"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.")
            "redirect:/users"
        }

    @PostMapping("/users/{id}")
    @PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @ModelAttribute("user") form: UserForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        validateUserForm(form, bindingResult, false)
        if (bindingResult.hasErrors()) {
            populateForm(model, form, false, formActionId = id)
            return "user/form"
        }

        return try {
            userService.updateUser(id, form)
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь обновлен.")
            "redirect:/users"
        } catch (ex: EntityNotFoundException) {
            bindingResult.reject("user.update", ex.message ?: "Связанные данные не найдены.")
            populateForm(model, form, false, formActionId = id)
            "user/form"
        } catch (ex: IllegalStateException) {
            bindingResult.reject("user.update", ex.message ?: "Не удалось обновить пользователя.")
            populateForm(model, form, false, formActionId = id)
            "user/form"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.reject("user.update", "Не удалось обновить пользователя. Логин должен быть уникальным.")
            populateForm(model, form, false, formActionId = id)
            "user/form"
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
    fun deleteUser(
        @PathVariable id: Long,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            userService.deleteUser(id, authentication.name)
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь удален.")
            "redirect:/users"
        } catch (ex: IllegalStateException) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.message)
            "redirect:/users"
        } catch (_: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить пользователя из-за связанных записей.")
            "redirect:/users"
        }

    private fun validateUserForm(form: UserForm, bindingResult: BindingResult, creating: Boolean) {
        if (creating && form.rawPassword.isBlank()) {
            bindingResult.rejectValue("rawPassword", "user.rawPassword", "Укажите пароль.")
        }

        if (form.rawPassword.isNotBlank() && form.rawPassword.trim().length < 4) {
            bindingResult.rejectValue("rawPassword", "user.rawPassword", "Пароль должен содержать минимум 4 символа.")
        }

        if (form.roleIds.isEmpty()) {
            bindingResult.rejectValue("roleIds", "user.roleIds", "Выберите хотя бы одну роль.")
        }
    }

    private fun populateForm(
        model: Model,
        form: UserForm,
        creating: Boolean,
        formActionId: Long? = form.id,
    ) {
        model.addAttribute("user", form)
        model.addAttribute("roles", userService.roleOptions())
        model.addAttribute("employees", employeeService.employeeOptions())
        model.addAttribute("pageTitle", if (creating) "Новый пользователь" else "Редактирование пользователя")
        model.addAttribute("submitLabel", if (creating) "Создать" else "Сохранить")
        model.addAttribute("formAction", if (creating) "/users" else "/users/$formActionId")
    }
}
