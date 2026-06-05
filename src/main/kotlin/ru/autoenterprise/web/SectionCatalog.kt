package ru.autoenterprise.web

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import ru.autoenterprise.security.AppRole

data class SectionDefinition(
    val slug: String,
    val title: String,
    val description: String,
    val currentStatus: String,
    val nextMilestone: String,
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
            currentStatus = "CRUD по транспорту, категориям, поступлению и выбытию уже работает",
            nextMilestone = "Следом: расширенные фильтры, история статусов и дополнительные проверки по жизненному циклу ТС.",
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
            currentStatus = "CRUD по сотрудникам уже подключен к БД",
            nextMilestone = "Следом: прием/увольнение, расширенные кадровые статусы и фильтры по составу персонала.",
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
            currentStatus = "CRUD по цехам, участкам, бригадам и назначениям уже работает",
            nextMilestone = "Следом: фильтры по оргструктуре, кадровые перемещения и сводные представления по подчиненности.",
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
            currentStatus = "CRUD по объектам гаражного хозяйства и размещению транспорта уже работает",
            nextMilestone = "Следом: контроль вместимости, фильтры по объектам и служебные представления по текущему размещению.",
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
            currentStatus = "CRUD по маршрутам уже работает",
            nextMilestone = "Следом: дополнительные фильтры по активности, сменам и эксплуатационной загрузке маршрутов.",
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
            currentStatus = "CRUD по назначениям на маршруты и эксплуатационным записям уже работает",
            nextMilestone = "Следом: фильтры по типам эксплуатации, сменам и подготовка отчетных выборок.",
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
            currentStatus = "CRUD по типам ремонта, журналу ремонтов и ремонтным работам уже работает",
            nextMilestone = "Следом: фильтры по статусам, исполнителям и расширенный просмотр состава ремонта.",
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
            currentStatus = "CRUD по агрегатам и истории их установки уже работает",
            nextMilestone = "Следом: фильтры по типам агрегатов, связка с ремонтной аналитикой и просмотр жизненного цикла.",
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
            currentStatus = "CRUD по пользователям уже подключен к БД",
            nextMilestone = "Следом: отдельное управление справочником ролей и аудит изменений доступа.",
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
            currentStatus = "Каталог и выполнение всех 29 SQL-отчетов уже работает",
            nextMilestone = "Следом: финальная доводка, дополнительные тесты и демонстрационный сценарий.",
            roles = allRoles,
            plannedArtifacts = listOf(
                "29 предметных отчетов из подготовленного набора SQL.",
                "Параметры периода, машины, категории, марки и сотрудника.",
                "Универсальный табличный вывод по alias-колонкам SQL.",
            ),
        ),
        SectionDefinition(
            slug = "sql-console",
            title = "SQL-консоль",
            description = "Технический раздел для SUPERADMIN.",
            currentStatus = "SELECT/WITH-only SQL-консоль для SUPERADMIN уже работает",
            nextMilestone = "Следом: финальная доводка, расширение проверок и демонстрационный сценарий проекта.",
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
