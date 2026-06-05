package ru.autoenterprise.security

data class DemoAccount(
    val username: String,
    val password: String,
    val roles: List<AppRole>,
    val description: String,
) {
    val rolesLabel: String
        get() = roles.joinToString(", ") { role -> role.roleName }
}

object DemoAccounts {
    val all: List<DemoAccount> = listOf(
        DemoAccount(
            username = "superadmin",
            password = "superadmin",
            roles = listOf(AppRole.SUPERADMIN),
            description = AppRole.SUPERADMIN.description,
        ),
        DemoAccount(
            username = "admin",
            password = "admin",
            roles = listOf(AppRole.ADMIN),
            description = AppRole.ADMIN.description,
        ),
        DemoAccount(
            username = "dispatcher",
            password = "dispatcher",
            roles = listOf(AppRole.DISPATCHER),
            description = AppRole.DISPATCHER.description,
        ),
        DemoAccount(
            username = "hr",
            password = "hr",
            roles = listOf(AppRole.HR),
            description = AppRole.HR.description,
        ),
        DemoAccount(
            username = "mechanic",
            password = "mechanic",
            roles = listOf(AppRole.MECHANIC),
            description = AppRole.MECHANIC.description,
        ),
        DemoAccount(
            username = "viewer",
            password = "viewer",
            roles = listOf(AppRole.VIEWER),
            description = AppRole.VIEWER.description,
        ),
    )
}
