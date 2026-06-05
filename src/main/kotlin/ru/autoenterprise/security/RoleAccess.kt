package ru.autoenterprise.security

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component("roleAccess")
class RoleAccess {

    fun has(authentication: Authentication?, role: AppRole): Boolean =
        hasAny(authentication, role)

    fun hasAny(authentication: Authentication?, vararg roles: AppRole): Boolean {
        if (authentication == null || authentication is AnonymousAuthenticationToken || !authentication.isAuthenticated) {
            return false
        }

        val authorities = authentication.authorities
            .map { authority -> authority.authority }
            .toSet()

        return roles.any { role -> role.authority in authorities }
    }
}
