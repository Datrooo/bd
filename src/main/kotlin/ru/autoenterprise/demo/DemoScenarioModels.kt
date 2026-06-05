package ru.autoenterprise.demo

import ru.autoenterprise.security.AppRole

data class DemoScenarioTrack(
    val title: String,
    val roles: List<AppRole>,
    val summary: String,
    val primaryPath: String,
    val primaryLabel: String,
    val steps: List<DemoScenarioStep>,
    val sampleSql: String? = null,
) {
    val rolesLabel: String
        get() = roles.joinToString(", ") { role -> role.roleName }
}

data class DemoScenarioStep(
    val title: String,
    val path: String,
    val description: String,
)
