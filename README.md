# Auto Enterprise

`Auto Enterprise` — учебная информационная система автопредприятия города.

Приложение покрывает учет автопарка, сотрудников, оргструктуры, гаражного хозяйства, маршрутов, эксплуатации транспорта, ремонтов, агрегатов, пользователей и ролей. Backend написан на `Kotlin + Spring Boot`, UI собран на `Thymeleaf + Bootstrap`, данные хранятся в `PostgreSQL`, а схема и бизнес-логика поднимаются через `Flyway`.

## Что умеет система

- DB-backed авторизация и роли через таблицы `app_user`, `role`, `user_role`
- CRUD по основным предметным модулям
- 28 SQL-отчетов из `queries.sql`
- read-only SQL-консоль только для `SUPERADMIN`
- error-страницы `403`, `404`, `500`

## Технологии

- `Java 21`
- `Kotlin 1.9.25`
- `Spring Boot 3.3.5`
- `Spring MVC`
- `Spring Security`
- `Spring Data JPA + Hibernate`
- `JdbcTemplate / NamedParameterJdbcTemplate`
- `Thymeleaf`
- `PostgreSQL 16`
- `Flyway`
- `Gradle Kotlin DSL`
- `Testcontainers`

## Требования

Для локального запуска нужны:

- `JDK 21`
- `Docker` и `Docker Compose`
- Unix-подобная shell-среда или терминал, из которого можно запускать `./gradlew`

## Структура проекта

- `src/main/kotlin` — backend-код приложения
- `src/main/resources/templates` — серверные HTML-шаблоны
- `src/main/resources/static` — CSS и статические ресурсы
- `src/main/resources/db/migration` — базовые Flyway-миграции
- `src/main/resources/db/local` — локальные dev-only миграции с seed-данными
- `src/main/resources/sql/reports` — runtime-копии 28 SQL-отчетов
- `config/application-local.example.yml` — пример локального конфига
- `compose.yaml` — локальный PostgreSQL

SQL-файлы в корне репозитория — это source pack предметной области:

- `ddl.sql`
- `triggers.sql`
- `seed.sql`
- `queries.sql`

Они не используются напрямую при старте приложения; рабочие миграции и runtime-SQL лежат в `src/main/resources`.

## Предметные модули

- `Транспорт`
- `Персонал`
- `Оргструктура`
- `Гаражное хозяйство`
- `Маршруты`
- `Перевозки и эксплуатация`
- `Ремонты`
- `Агрегаты`
- `Пользователи и роли`
- `Отчеты`
- `SQL-консоль`

## Роли

- `SUPERADMIN` — полный доступ ко всем разделам, включая SQL-консоль
- `ADMIN` — администрирование предметных данных и пользователей, без SQL-консоли
- `DISPATCHER` — маршруты, перевозки, закрепления, эксплуатация
- `HR` — сотрудники и оргструктура
- `MECHANIC` — ремонты и агрегаты
- `VIEWER` — просмотр разделов и отчетов без административных функций

## Сборка

Собрать проект:

```bash
3
```

Собрать исполняемый jar:

```bash
./gradlew bootJar
```

После этого артефакт будет лежать в `build/libs/`.

## Быстрый локальный запуск

### 1. Поднять PostgreSQL

```bash
docker compose up -d
```

Если локальный volume уже был создан старой версией проекта, перед первым запуском после обновления нужно один раз пересоздать БД, чтобы сработали `initdb`-скрипты с runtime-пользователями:

```bash
docker compose down -v
docker compose up -d
```

По умолчанию контейнер поднимается с такими параметрами:

- host: `localhost`
- port: `5433`
- database: `auto_enterprise`
- migration user: `postgres / postgres`
- app runtime user: `auto_enterprise_app / auto_enterprise_app`
- SQL console user: `auto_enterprise_sql_console / auto_enterprise_sql_console`

### 2. Создать локальный конфиг

```bash
cp config/application-local.example.yml config/application-local.yml
```

Этот файл автоматически подхватывается через:

```yaml
spring.config.import=optional:file:./config/application-local.yml
```

Локальный конфиг:

- включает подключение приложения под ограниченным runtime-пользователем
- отдельно задает `Flyway`-подключение под `postgres` для миграций
- отдельно задает read-only datasource для SQL-консоли
- добавляет `classpath:db/local`
- поднимает demo-seed
- включает отображение dev-учеток на login-странице

### 3. Прогнать тесты

```bash
./gradlew test
```

### 4. Запустить приложение

```bash
./gradlew bootRun
```

После старта приложение будет доступно по адресу:

- `http://localhost:8080`
- `http://localhost:8080/login`

## Локальные dev-учетки

При запуске с `config/application-local.yml` доступны следующие учетные записи:

- `superadmin / superadmin`
- `admin / admin`
- `dispatcher / dispatcher`
- `hr / hr`
- `mechanic / mechanic`
- `viewer / viewer`

## Как пользоваться приложением

### Вход

1. Открой `http://localhost:8080/login`
2. Войди под одной из dev-учеток
3. После логина приложение перенаправит на `/dashboard`

### Обзор

Раздел `/dashboard` показывает:

- общая сводка проекта
- доступные разделы для текущей роли
- быстрые переходы в рабочие модули

### CRUD-разделы

Через верхнее меню доступны основные рабочие разделы:

- `/vehicles`
- `/employees`
- `/organization`
- `/garage`
- `/routes`
- `/transportation`
- `/repairs`
- `/components`
- `/users` — только `SUPERADMIN`, `ADMIN`

Во всех CRUD-модулях доступны списки, формы создания/редактирования и удаление в рамках ролевой модели.

### Отчеты

Раздел `/reports` содержит каталог из 28 SQL-отчетов.

Что важно:

- часть отчетов выполняется сразу
- часть требует параметры: период, категория, марка, сотрудник, машина
- результаты показываются как таблица по alias-колонкам SQL
- каждый отчет ограничен по времени выполнения и по максимальному числу строк результата

Примеры:

- `/reports/r01` — отчет без параметров
- `/reports/r18` — отчет по использованным агрегатам с параметрами

### SQL-консоль

Раздел `/sql-console` доступен только `SUPERADMIN`.

Ограничения:

- только один SQL statement
- только `SELECT`
- только прикладные таблицы и безопасные SQL-функции
- системные namespace PostgreSQL недоступны
- лимит результата: 200 строк
- timeout: 10 секунд
- используется отдельный ограниченный DB-пользователь
- соединение открывается в режиме `readOnly`
- модифицирующие, DDL и административные команды отклоняются

Пример безопасного запроса:

```sql
SELECT id, username, is_active, last_login_at
FROM app_user
ORDER BY id;
```

## Короткий demo-маршрут

1. Войти под `viewer / viewer` и показать `/dashboard`, `/vehicles`, `/reports/r01`
2. Войти под `dispatcher / dispatcher` и показать `/transportation-records`, затем `/reports/r17`
3. Войти под `mechanic / mechanic` и показать `/repairs-journal`, `/component-history`
4. Войти под `admin / admin` и показать `/users`, затем подтвердить запрет на `/sql-console`
5. Войти под `superadmin / superadmin` и выполнить read-only запрос в `/sql-console`

## Тесты

Проект использует:

- обычные Spring Boot тесты
- интеграционные тесты с `Testcontainers + PostgreSQL`

Основная команда:

```bash
./gradlew test
```

Docker для интеграционных тестов обязателен. Если контейнерный runtime недоступен, `./gradlew test` должен падать, а не маскировать проблему через `skipped`.

### Docker Desktop на macOS

Если `Testcontainers` не может подключиться к Docker Desktop, используй:

```bash
DOCKER_HOST=unix://$HOME/Library/Containers/com.docker.docker/Data/docker.raw.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
./gradlew test
```

## Production-like режим

Если не создавать `config/application-local.yml`, приложение использует только:

- базовые Flyway-миграции из `src/main/resources/db/migration`
- production-safe конфиг без demo-seed

Это значит:

- таблицы будут созданы
- локальные dev-учетки не появятся
- `db/local` не будет применен
- пользователей для входа нужно будет создать самостоятельно в `app_user` и `user_role`

## Полезные файлы

- `compose.yaml` — локальная БД
- `config/application-local.example.yml` — пример локального конфига
- `src/main/resources/application.yml` — базовый runtime-конфиг
- `src/main/resources/db/migration` — схема и триггеры
- `src/main/resources/db/local` — локальный seed
- `src/main/resources/sql/reports` — SQL отчетов

## Возможные проблемы

### Порт `5433` занят

Либо освободи порт, либо измени `compose.yaml` и `config/application-local.yml` согласованно.

### Не удается войти в систему

Проверь:

- что создан `config/application-local.yml`
- что PostgreSQL поднят
- что Flyway применил `db/local`
- что используешь одну из dev-учеток из списка выше

### Приложение стартует, но логин-страница не показывает dev-учетки

Это означает, что `app.security.show-dev-accounts=false` и локальный конфиг не подключен.

## Лицензия и назначение

Проект собран как учебная курсовая информационная система с акцентом на:

- предметную модель
- ролевой доступ
- SQL-отчеты
- воспроизводимый локальный запуск
