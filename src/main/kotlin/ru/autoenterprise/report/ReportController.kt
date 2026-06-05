package ru.autoenterprise.report

import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("/reports")
@PreAuthorize("isAuthenticated()")
class ReportController(
    private val reportService: ReportService,
) {

    @GetMapping
    fun catalog(model: Model): String {
        model.addAttribute("groups", reportService.catalogGroups())
        return "report/catalog"
    }

    @GetMapping("/{code}")
    fun detail(
        @PathVariable code: String,
        model: Model,
    ): String {
        val report = loadReport(code)
        val request = ReportRequestForm()
        var tableResult: ReportTableResult? = null
        var errorMessage: String? = null

        if (!report.requiresParameters) {
            try {
                tableResult = reportService.execute(report, request.toExecutionRequest(report.code))
            } catch (_: DataAccessException) {
                errorMessage = "Не удалось выполнить SQL-отчет. Проверьте доступность базы и корректность seed-данных."
            }
        }

        populateDetailModel(model, report, request, tableResult, errorMessage)
        return "report/detail"
    }

    @PostMapping("/{code}")
    fun execute(
        @PathVariable code: String,
        @ModelAttribute("request") request: ReportRequestForm,
        bindingResult: BindingResult,
        model: Model,
    ): String {
        val report = loadReport(code)
        validateRequest(report, request, bindingResult)

        var tableResult: ReportTableResult? = null
        var errorMessage: String? = null

        if (!bindingResult.hasErrors()) {
            try {
                tableResult = reportService.execute(report, request.toExecutionRequest(report.code))
            } catch (_: DataAccessException) {
                errorMessage = "Не удалось выполнить SQL-отчет. Проверьте параметры и состояние БД."
            }
        }

        populateDetailModel(model, report, request, tableResult, errorMessage)
        return "report/detail"
    }

    private fun populateDetailModel(
        model: Model,
        report: ReportDefinition,
        request: ReportRequestForm,
        tableResult: ReportTableResult?,
        errorMessage: String?,
    ) {
        val referenceData = reportService.referenceData()

        model.addAttribute("report", report)
        model.addAttribute("pageTitle", "${report.code.uppercase()} · ${report.title}")
        model.addAttribute("request", request)
        model.addAttribute("vehicles", referenceData.vehicles)
        model.addAttribute("employees", referenceData.employees)
        model.addAttribute("categories", referenceData.categories)
        model.addAttribute("brands", referenceData.brands)
        model.addAttribute("tableResult", tableResult)
        model.addAttribute("errorMessage", errorMessage)
    }

    private fun validateRequest(
        report: ReportDefinition,
        request: ReportRequestForm,
        bindingResult: BindingResult,
    ) {
        if (report.requiresVehicle && request.vehicleId == null) {
            bindingResult.rejectValue("vehicleId", "required", "Выберите транспорт.")
        }
        if (report.requiresEmployee && request.employeeId == null) {
            bindingResult.rejectValue("employeeId", "required", "Выберите сотрудника.")
        }
        if (report.requiresCategory && request.categoryName.isNullOrBlank()) {
            bindingResult.rejectValue("categoryName", "required", "Выберите категорию транспорта.")
        }
        if (report.requiresBrand && request.brandName.isNullOrBlank()) {
            bindingResult.rejectValue("brandName", "required", "Выберите марку транспорта.")
        }
        if (report.requiresDateRange && request.startDate == null) {
            bindingResult.rejectValue("startDate", "required", "Укажите дату начала периода.")
        }
        if (report.requiresDateRange && request.endDate == null) {
            bindingResult.rejectValue("endDate", "required", "Укажите дату окончания периода.")
        }
    }

    private fun loadReport(code: String): ReportDefinition =
        reportService.findReport(code)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Отчет не найден.")
}
