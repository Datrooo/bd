package ru.autoenterprise.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.autoenterprise.security.AppRole
import ru.autoenterprise.security.DemoAccounts

@Controller
@RequestMapping("/demo-scenario")
class DemoScenarioController(
    @Value("\${app.security.show-dev-accounts:false}")
    private val showDevAccounts: Boolean,
) {

    @GetMapping
    fun scenario(authentication: Authentication, model: Model): String {
        val currentRoles = authentication.authorities
            .mapNotNull { authority -> AppRole.fromAuthority(authority.authority) }
            .toSet()

        model.addAttribute("pageTitle", "Демо-сценарий")
        model.addAttribute("recommendedTracks", tracks.filter { track -> track.roles.any(currentRoles::contains) })
        model.addAttribute("tracks", tracks)
        model.addAttribute("showDevAccounts", showDevAccounts)
        if (showDevAccounts) {
            model.addAttribute("demoAccounts", DemoAccounts.all)
        }
        return "demo/scenario"
    }

    companion object {
        private val tracks = listOf(
            DemoScenarioTrack(
                title = "Обзор для диспетчера",
                roles = listOf(AppRole.DISPATCHER),
                summary = "Показать маршруты, назначения транспорта и эксплуатационные записи, затем подтвердить цифры отчетом по выручке и пассажиропотоку.",
                primaryPath = "/transportation",
                primaryLabel = "Открыть перевозки",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Проверить закрепления на маршрутах",
                        path = "/route-vehicle-assignments",
                        description = "Открыть список назначений и показать, что транспорт уже связан с маршрутами и периодами действия.",
                    ),
                    DemoScenarioStep(
                        title = "Показать эксплуатационные записи",
                        path = "/transportation-records",
                        description = "Открыть журнал выходов на линию, пробег, пассажиров и выручку по конкретным рейсам.",
                    ),
                    DemoScenarioStep(
                        title = "Подтвердить агрегированную картину отчетом",
                        path = "/reports/r29",
                        description = "Запустить отчет по маршрутам за апрель 2026 года и сверить итог по пассажирам и выручке.",
                    ),
                ),
            ),
            DemoScenarioTrack(
                title = "Кадровый контур для HR",
                roles = listOf(AppRole.HR),
                summary = "Показать сотрудников, оргструктуру и назначения, затем вывести подчиненность и состав бригад через отчеты.",
                primaryPath = "/employees",
                primaryLabel = "Открыть персонал",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Открыть кадровый список",
                        path = "/employees",
                        description = "Показать сотрудников, должности и текущие кадровые данные по предприятию.",
                    ),
                    DemoScenarioStep(
                        title = "Проверить оргструктуру",
                        path = "/organization",
                        description = "Перейти в цеха, участки и бригады, затем открыть назначения сотрудников по бригадам.",
                    ),
                    DemoScenarioStep(
                        title = "Вывести подчиненность в отчетах",
                        path = "/reports/r24",
                        description = "Открыть отчет по подчиненным сотрудника и показать, что данные из оргструктуры сходятся с CRUD-разделом.",
                    ),
                ),
            ),
            DemoScenarioTrack(
                title = "Ремонтный контур для механика",
                roles = listOf(AppRole.MECHANIC),
                summary = "Показать журнал ремонтов, состав работ и историю агрегатов, затем подтвердить результат тематическими отчетами.",
                primaryPath = "/repairs",
                primaryLabel = "Открыть ремонты",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Проверить журнал ремонтов",
                        path = "/repairs-journal",
                        description = "Открыть ремонтные карточки, даты, типы и уже пересчитанную суммарную стоимость.",
                    ),
                    DemoScenarioStep(
                        title = "Показать выполненные работы",
                        path = "/repair-works",
                        description = "Открыть состав работ и показать, что total_cost поддерживается триггерами БД.",
                    ),
                    DemoScenarioStep(
                        title = "Открыть историю агрегатов",
                        path = "/component-history",
                        description = "Показать установку, снятие и текущий статус агрегатов на транспорте.",
                    ),
                ),
            ),
            DemoScenarioTrack(
                title = "Операционное администрирование",
                roles = listOf(AppRole.ADMIN),
                summary = "Показать пользователей, роли и базовые предметные справочники без доступа к SQL-консоли.",
                primaryPath = "/users",
                primaryLabel = "Открыть пользователей",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Проверить учетные записи",
                        path = "/users",
                        description = "Открыть список app_user, роли и флаг активности, показать DB-backed авторизацию.",
                    ),
                    DemoScenarioStep(
                        title = "Открыть автопарк и отчеты",
                        path = "/vehicles",
                        description = "Показать, что операционный администратор работает с предметными CRUD-разделами и аналитикой.",
                    ),
                    DemoScenarioStep(
                        title = "Подтвердить отсутствие доступа к SQL-консоли",
                        path = "/sql-console",
                        description = "При попытке перехода система должна вернуть 403, сохранив изоляцию SUPERADMIN-функций.",
                    ),
                ),
            ),
            DemoScenarioTrack(
                title = "Read-only показ для наблюдателя",
                roles = listOf(AppRole.VIEWER),
                summary = "Короткий сценарий демонстрации интерфейса без прав на изменение: обзор, справочники и отчеты.",
                primaryPath = "/dashboard",
                primaryLabel = "Открыть обзор",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Показать обзор модулей",
                        path = "/dashboard",
                        description = "Открыть дашборд и быстро пройтись по составу проекта и роли текущего пользователя.",
                    ),
                    DemoScenarioStep(
                        title = "Открыть транспорт и персонал",
                        path = "/vehicles",
                        description = "Показать list-страницы без доступа к административным операциям и SQL-консоли.",
                    ),
                    DemoScenarioStep(
                        title = "Открыть отчет без параметров",
                        path = "/reports/r01",
                        description = "Показать готовый табличный отчет по текущему автопарку без изменения данных.",
                    ),
                ),
            ),
            DemoScenarioTrack(
                title = "Системный контроль SUPERADMIN",
                roles = listOf(AppRole.SUPERADMIN),
                summary = "Финальный технический сценарий: проверить пользователей, отчеты и read-only SQL-консоль поверх той же схемы данных.",
                primaryPath = "/sql-console",
                primaryLabel = "Открыть SQL-консоль",
                steps = listOf(
                    DemoScenarioStep(
                        title = "Проверить пользователи и роли",
                        path = "/users",
                        description = "Открыть администрирование доступа и убедиться, что роли читаются из БД, а last_login_at обновляется после логина.",
                    ),
                    DemoScenarioStep(
                        title = "Сверить аналитику через отчеты",
                        path = "/reports/r29",
                        description = "Открыть отчет по маршрутам и сравнить сводные числа с эксплуатационными записями.",
                    ),
                    DemoScenarioStep(
                        title = "Выполнить технический read-only запрос",
                        path = "/sql-console",
                        description = "Выполнить безопасный SELECT к app_user или vehicle и показать защиту от модифицирующих команд.",
                    ),
                ),
                sampleSql = "SELECT id, username, is_active, last_login_at FROM app_user ORDER BY id;",
            ),
        )
    }
}
