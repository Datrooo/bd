package ru.autoenterprise.web

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import ru.autoenterprise.security.AppRole

data class SectionDefinition(
    val slug: String,
    val title: String,
    val description: String,
    val roles: List<AppRole>,
    val plannedArtifacts: List<String>,
) {
    val path: String
        get() = "/$slug"

    val rolesLabel: String
        get() = roles.joinToString(", ") { role -> role.roleName }
}

@Component
class SectionCatalog {

    private val allRoles = AppRole.entries

    private val sections: List<SectionDefinition> = listOf(
        SectionDefinition(
            slug = "vehicles",
            title = "Транспорт",
            description = "Автопарк, категории, поступление и выбытие техники.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Справочники категорий и статусов транспорта.",
                "Карточка ТС с историей поступления и выбытия.",
                "Пагинация и фильтрация по марке, категории и состоянию.",
            ),
        ),
        SectionDefinition(
            slug = "employees",
            title = "Персонал",
            description = "Сотрудники предприятия и кадровые данные.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Списки сотрудников с поиском по ФИО и должности.",
                "Формы приема, увольнения и статусов сотрудников.",
                "Связка сотрудников с учетными записями приложения.",
            ),
        ),
        SectionDefinition(
            slug = "organization",
            title = "Оргструктура",
            description = "Цеха, участки, бригады и назначения сотрудников.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Иерархия цех -> участок -> бригада.",
                "Закрепление сотрудников за бригадами.",
                "Просмотр подчиненности для мастеров и начальников цехов.",
            ),
        ),
        SectionDefinition(
            slug = "garage",
            title = "Гаражное хозяйство",
            description = "Гаражи, боксы, стоянки и история размещения транспорта.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Список объектов гаражного хозяйства.",
                "История размещения каждой машины.",
                "Контроль вместимости и привязка к цехам/участкам.",
            ),
        ),
        SectionDefinition(
            slug = "routes",
            title = "Маршруты",
            description = "Маршруты и закрепление транспорта за маршрутами.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Карточки маршрутов с типом и протяженностью.",
                "Назначение транспорта на маршрут и смены.",
                "Учет триггера соответствия route_type и категории ТС.",
            ),
        ),
        SectionDefinition(
            slug = "transportation",
            title = "Эксплуатация",
            description = "Записи о пассажирских, грузовых и сервисных перевозках.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Формы ввода записей по типам эксплуатации.",
                "Подсчет пробега и пассажиропотока.",
                "Отработка триггеров consistency для PASSENGER/CARGO/SERVICE.",
            ),
        ),
        SectionDefinition(
            slug = "repairs",
            title = "Ремонты",
            description = "Ремонты транспорта и выполненные ремонтные работы.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Журнал ремонтов по технике.",
                "Состав и стоимость выполненных работ.",
                "Автопересчет total_cost через БД-триггеры.",
            ),
        ),
        SectionDefinition(
            slug = "components",
            title = "Агрегаты",
            description = "Склад агрегатов и история их установки/снятия.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "Учет агрегатов по серийным номерам.",
                "История установки, ремонта, замены и снятия.",
                "Контроль статусов и бизнес-логики триггерами.",
            ),
        ),
        SectionDefinition(
            slug = "users",
            title = "Пользователи и роли",
            description = "Управление app_user, role и user_role.",
            roles = listOf(AppRole.SUPERADMIN, AppRole.ADMIN),
            plannedArtifacts = listOf(
                "Список учетных записей и привязка к employee.",
                "Назначение ролей и блокировка пользователей.",
                "Редактирование учетных записей без временных in-memory пользователей.",
            ),
        ),
        SectionDefinition(
            slug = "reports",
            title = "Отчеты",
            description = "Отдельный модуль для SQL-отчетов из queries.sql.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "28 предметных отчетов из подготовленного набора SQL.",
                "Параметры периода, машины, категории, марки, типа агрегата и сотрудника.",
                "Универсальный табличный вывод по alias-колонкам SQL.",
            ),
        ),
        SectionDefinition(
            slug = "sql-console",
            title = "SQL-консоль",
            description = "Технический раздел для SUPERADMIN.",
            roles = listOf(AppRole.SUPERADMIN),
            plannedArtifacts = listOf(
                "Изолированная форма выполнения произвольного read-only SQL.",
                "Лимит строк, таймаут и readOnly-режим подключения.",
                "Логирование попыток выполнения и политика безопасной фильтрации команд.",
            ),
        ),
    )

    fun all(): List<SectionDefinition> = sections

    fun visibleTo(authentication: Authentication?): List<SectionDefinition> {
        if (authentication == null) {
            return emptyList()
        }

        val roles = authentication.authorities
            .mapNotNull { authority -> AppRole.fromAuthority(authority.authority) }
            .toSet()

        return sections.filter { section -> section.roles.any(roles::contains) }
    }

    fun findBySlug(slug: String): SectionDefinition? =
        sections.firstOrNull { section -> section.slug == slug }
}
