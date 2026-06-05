package ru.autoenterprise.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController(
    @Value("\${app.security.show-dev-accounts:false}")
    private val showDevAccounts: Boolean,
) {

    @GetMapping("/login")
    fun login(model: Model): String {
        model.addAttribute("showDevAccounts", showDevAccounts)
        if (showDevAccounts) {
            model.addAttribute("demoAccounts", DemoAccounts.all)
        }
        return "security/login"
    }
}
