package ru.autoenterprise.security

import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.stereotype.Component

@Component
class InteractiveAuthenticationSuccessListener(
    private val accountRepository: DatabaseUserAccountRepository,
) {

    @EventListener
    fun handle(event: InteractiveAuthenticationSuccessEvent) {
        accountRepository.updateLastLogin(event.authentication.name)
    }
}
