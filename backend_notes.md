# Рекомендации по backend-структуре

## Согласованный стек
- PostgreSQL
- Kotlin
- Spring Boot
- Spring MVC
- Spring Data JPA + Hibernate
- JdbcTemplate / NamedParameterJdbcTemplate для сложных SQL-запросов
- Spring Security
- Thymeleaf
- Bootstrap 5
- Flyway

## Предлагаемая структура пакетов
- `config`
- `security`
- `user`
- `vehicle`
- `employee`
- `organization`
- `garage`
- `route`
- `transportation`
- `repair`
- `component`
- `report`
- `common`

## Подход к реализации
- Простые CRUD-операции: JPA entities + repositories + services + controllers
- Сложные отчёты и аналитика: SQL через JdbcTemplate
- UI: server-side HTML через Thymeleaf

## Security
Роли проверять через Spring Security:
- `hasRole("SUPERADMIN")`
- `hasRole("ADMIN")`
- `hasRole("DISPATCHER")`
- `hasRole("HR")`
- `hasRole("MECHANIC")`
- `hasRole("VIEWER")`

## Что лучше сделать как отдельные модули/разделы UI
- Пользователи и роли
- Транспорт
- Персонал и оргструктура
- Гаражное хозяйство
- Маршруты
- Эксплуатация / перевозки
- Ремонты
- Агрегаты
- Отчёты
- SQL-консоль (только SUPERADMIN)
