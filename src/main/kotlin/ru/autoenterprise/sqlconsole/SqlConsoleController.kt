package ru.autoenterprise.sqlconsole

import java.sql.SQLException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/sql-console")
@PreAuthorize("@roleAccess.has(authentication, T(ru.autoenterprise.security.AppRole).SUPERADMIN)")
class SqlConsoleController(
    private val sqlConsoleService: SqlConsoleService,
) {

    @GetMapping
    fun console(model: Model): String {
        populateModel(model, SqlConsoleRequestForm(), null, null)
        return "sqlconsole/index"
    }

    @PostMapping
    fun execute(
        @ModelAttribute("sqlConsole") form: SqlConsoleRequestForm,
        bindingResult: BindingResult,
        authentication: Authentication,
        model: Model,
    ): String {
        var executionResult: SqlConsoleExecutionResult? = null
        var errorMessage: String? = null

        if (form.sql.isBlank()) {
            bindingResult.rejectValue("sql", "required", "Введите SQL-запрос.")
        }

        if (!bindingResult.hasErrors()) {
            try {
                executionResult = sqlConsoleService.execute(form.sql, authentication.name)
            } catch (ex: SqlConsoleQueryRejectedException) {
                bindingResult.rejectValue("sql", "invalid", ex.message ?: "Запрос отклонен.")
            } catch (ex: SQLException) {
                errorMessage = "Не удалось выполнить SQL-запрос в рамках политики read-only консоли."
            }
        }

        populateModel(model, form, executionResult, errorMessage)
        return "sqlconsole/index"
    }

    private fun populateModel(
        model: Model,
        form: SqlConsoleRequestForm,
        executionResult: SqlConsoleExecutionResult?,
        errorMessage: String?,
    ) {
        model.addAttribute("pageTitle", "SQL-консоль")
        model.addAttribute("sqlConsole", form)
        model.addAttribute("executionResult", executionResult)
        model.addAttribute("errorMessage", errorMessage)
    }
}
