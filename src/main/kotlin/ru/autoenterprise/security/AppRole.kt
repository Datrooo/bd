package ru.autoenterprise.security

enum class AppRole(
    val description: String,
) {
    SUPERADMIN("Полный доступ ко всем разделам, включая SQL-консоль."),
    ADMIN("Операционный администратор предметных данных."),
    DISPATCHER("Маршруты, перевозки, закрепления транспорта и водителей."),
    HR("Кадровый контур и оргструктура."),
    MECHANIC("Ремонты, работы и история агрегатов."),
    VIEWER("Только просмотр разделов и отчетов."),
    ;

    val roleName: String
        get() = name

    val authority: String
        get() = "ROLE_$name"

    companion object {
        fun fromRoleName(value: String): AppRole =
            entries.firstOrNull { role -> role.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Неизвестная роль '$value'.")

        fun fromAuthority(value: String): AppRole? =
            value.removePrefix("ROLE_")
                .takeIf(String::isNotBlank)
                ?.let { roleName ->
                    entries.firstOrNull { role -> role.name.equals(roleName, ignoreCase = true) }
                }
    }
}
