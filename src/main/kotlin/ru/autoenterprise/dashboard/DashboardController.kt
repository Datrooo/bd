package ru.autoenterprise.dashboard

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import ru.autoenterprise.web.SectionCatalog

@Controller
class DashboardController(
    private val sectionCatalog: SectionCatalog,
) {

    @GetMapping("/")
    fun root(): String = "redirect:/dashboard"

    @GetMapping("/dashboard")
    fun dashboard(authentication: Authentication, model: Model): String {
        model.addAttribute("sections", sectionCatalog.visibleTo(authentication))
        return "dashboard/index"
    }
}
