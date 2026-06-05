package ru.autoenterprise.security

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class DatabaseUserDetailsService(
    private val accountRepository: DatabaseUserAccountRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val account = accountRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User '$username' not found")

        return User.withUsername(account.username)
            .password(account.passwordHash)
            .disabled(!account.active)
            .roles(*account.roles.map(AppRole::roleName).toTypedArray())
            .build()
    }
}
