# Роли, use case’ы и матрица доступа

## Роли
- `SUPERADMIN`
- `ADMIN`
- `DISPATCHER`
- `HR`
- `MECHANIC`
- `VIEWER`

## Смысл ролей

### SUPERADMIN
Полный контроль над системой:
- управление пользователями;
- назначение ролей;
- доступ ко всем разделам;
- выполнение сырых SQL-запросов;
- полный CRUD по всем сущностям.

### ADMIN
Операционное администрирование предметных данных:
- транспорт;
- маршруты;
- гаражное хозяйство;
- справочники;
- просмотр отчётов.

### DISPATCHER
Работа с эксплуатацией транспорта:
- маршруты;
- закрепление транспорта за маршрутами;
- эксплуатационные записи;
- перевозки;
- пробег;
- распределение водителей по машинам.

### HR
Кадровая роль:
- сотрудники;
- бригады;
- оргструктура;
- кадровые данные;
- распределение сотрудников по бригадам.

### MECHANIC
Ремонтно-техническая роль:
- ремонты;
- работы по ремонту;
- агрегаты;
- история агрегатов;
- техническая история транспорта.

### VIEWER
Только чтение и просмотр отчётов.

## Матрица доступа
Обозначения:
- `R` — read
- `C` — create
- `U` — update
- `D` — delete

### Транспорт и справочники
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| vehicle_category | R C U D | R C U D | R | R | R | R |
| vehicle | R C U D | R C U D | R U | R | R U | R |
| vehicle_acquisition | R C U D | R C U D | R | R | R | R |
| vehicle_disposal | R C U D | R C U D | R | R | R | R |

### Персонал и оргструктура
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| employee | R C U D | R U | R | R C U D | R | R |
| workshop | R C U D | R C U D | R | R U | R | R |
| section | R C U D | R C U D | R | R U | R | R |
| brigade | R C U D | R C U D | R | R U | R | R |
| employee_brigade_assignment | R C U D | R U | R | R C U D | R | R |
| vehicle_driver_assignment | R C U D | R C U D | R C U D | R | R | R |

### Гаражное хозяйство
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| garage_object | R C U D | R C U D | R | R | R | R |
| vehicle_location_history | R C U D | R C U D | R C U | R | R U | R |

### Маршруты и эксплуатация
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| route | R C U D | R C U D | R C U D | R | R | R |
| route_vehicle_assignment | R C U D | R C U D | R C U D | R | R | R |
| transportation_record | R C U D | R C U D | R C U D | R | R | R |

### Ремонты и агрегаты
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| repair_type | R C U D | R C U D | R | R | R | R |
| repair | R C U D | R C U D | R | R | R C U D | R |
| repair_work | R C U D | R C U D | R | R | R C U D | R |
| component | R C U D | R C U D | R | R | R C U D | R |
| vehicle_component_history | R C U D | R C U D | R | R | R C U D | R |

### Пользователи и роли
| Сущность | SUPERADMIN | ADMIN | DISPATCHER | HR | MECHANIC | VIEWER |
|---|---|---|---|---|---|---|
| app_user | R C U D | R | - | - | - | - |
| role | R C U D | R | - | - | - | - |
| user_role | R C U D | R | - | - | - | - |

## Важное замечание
Это **упрощённая RBAC-модель**, а не полный ACL.

В БД хранятся:
- роли;
- пользователи;
- связи пользователей с ролями.

Проверка доступа к use case’ам предполагается на backend через Spring Security по ролям пользователя.
