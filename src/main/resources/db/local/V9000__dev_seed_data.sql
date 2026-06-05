-- =========================================================
-- SEED-ДАННЫЕ
-- =========================================================

INSERT INTO role (name, description) VALUES
('SUPERADMIN', 'Суперадминистратор системы'),
('ADMIN', 'Администратор системы'),
('DISPATCHER', 'Диспетчер'),
('HR', 'Кадровый сотрудник'),
('MECHANIC', 'Механик / сотрудник ремонтной службы'),
('VIEWER', 'Пользователь только для просмотра');

INSERT INTO vehicle_category (name, description) VALUES
('BUS', 'Автобус'),
('MINIBUS', 'Маршрутное такси'),
('TAXI', 'Такси'),
('PASSENGER_CAR', 'Легковой служебный транспорт'),
('CARGO', 'Грузовой транспорт'),
('SERVICE', 'Вспомогательный транспорт');

INSERT INTO repair_type (name, description) VALUES
('CURRENT', 'Текущий ремонт'),
('CAPITAL', 'Капитальный ремонт'),
('EMERGENCY', 'Аварийный ремонт'),
('PREVENTIVE', 'Профилактическое обслуживание');

INSERT INTO employee (
    personnel_number, last_name, first_name, middle_name, birth_date,
    hire_date, dismissal_date, position, qualification, phone, email,
    address, status, notes
) VALUES
('EMP-001', 'Иванов', 'Иван', 'Иванович', '1980-05-10', '2015-03-01', NULL, 'WORKSHOP_CHIEF', 'Высшая категория', '+79990000001', 'ivanov@example.com', 'г. Город, ул. Центральная, 1', 'ACTIVE', 'Начальник цеха №1'),
('EMP-002', 'Петров', 'Пётр', 'Петрович', '1985-07-12', '2017-06-15', NULL, 'MASTER', 'Мастер участка', '+79990000002', 'petrov@example.com', 'г. Город, ул. Южная, 5', 'ACTIVE', 'Мастер участка ремонта'),
('EMP-003', 'Сидоров', 'Сидор', 'Сидорович', '1988-04-03', '2019-02-10', NULL, 'BRIGADIER', 'Бригадир ремонтной бригады', '+79990000003', 'sidorov@example.com', 'г. Город, ул. Лесная, 7', 'ACTIVE', 'Бригадир бригады №1'),
('EMP-004', 'Смирнов', 'Алексей', 'Николаевич', '1990-09-20', '2020-01-11', NULL, 'DRIVER', 'Водитель автобуса', '+79990000004', 'smirnov@example.com', 'г. Город, ул. Мира, 10', 'ACTIVE', NULL),
('EMP-005', 'Кузнецов', 'Дмитрий', 'Сергеевич', '1992-11-05', '2021-04-01', NULL, 'DRIVER', 'Водитель маршрутного такси', '+79990000005', 'kuznetsov@example.com', 'г. Город, ул. Полевая, 8', 'ACTIVE', NULL),
('EMP-006', 'Волков', 'Олег', 'Андреевич', '1987-08-14', '2018-09-17', NULL, 'DRIVER', 'Водитель грузового транспорта', '+79990000006', 'volkov@example.com', 'г. Город, ул. Набережная, 15', 'ACTIVE', NULL),
('EMP-007', 'Фёдоров', 'Игорь', 'Викторович', '1983-01-25', '2016-05-20', NULL, 'WELDER', 'Сварщик 5 разряда', '+79990000007', 'fedorov@example.com', 'г. Город, ул. Заводская, 20', 'ACTIVE', NULL),
('EMP-008', 'Морозов', 'Сергей', 'Павлович', '1986-03-18', '2014-10-10', NULL, 'LOCKSMITH', 'Слесарь по ремонту автомобилей', '+79990000008', 'morozov@example.com', 'г. Город, ул. Восточная, 12', 'ACTIVE', NULL),
('EMP-009', 'Орлова', 'Анна', 'Игоревна', '1991-06-30', '2022-01-15', NULL, 'DISPATCHER', 'Диспетчер транспортного отдела', '+79990000009', 'orlova@example.com', 'г. Город, ул. Садовая, 3', 'ACTIVE', NULL),
('EMP-010', 'Соколова', 'Мария', 'Олеговна', '1993-02-22', '2023-03-01', NULL, 'HR', 'Специалист по кадрам', '+79990000010', 'sokolova@example.com', 'г. Город, ул. Школьная, 9', 'ACTIVE', NULL);

INSERT INTO workshop (name, chief_employee_id, description)
VALUES ('Цех технического обслуживания', 1, 'Основной цех по ремонту и обслуживанию транспорта');

INSERT INTO section (workshop_id, name, master_employee_id, description)
VALUES (1, 'Участок ремонта двигателей и ходовой части', 2, 'Участок выполнения основных ремонтных работ');

INSERT INTO brigade (section_id, name, brigadier_employee_id, description)
VALUES (1, 'Ремонтная бригада №1', 3, 'Бригада текущего и аварийного ремонта');

INSERT INTO employee_brigade_assignment (employee_id, brigade_id, start_date, end_date, is_primary) VALUES
(3, 1, '2023-01-01', NULL, TRUE),
(7, 1, '2023-01-01', NULL, TRUE),
(8, 1, '2023-01-01', NULL, TRUE);

INSERT INTO garage_object (
    name, object_type, parent_object_id, workshop_id, section_id, address, capacity, description
) VALUES
('Главный гараж', 'GARAGE', NULL, 1, NULL, 'г. Город, ул. Промышленная, 1', 50, 'Основной гараж предприятия'),
('Бокс №1', 'BOX', 1, 1, 1, 'г. Город, ул. Промышленная, 1', 2, 'Бокс для диагностики и ремонта'),
('Ремонтный корпус', 'REPAIR_BUILDING', NULL, 1, 1, 'г. Город, ул. Промышленная, 2', 10, 'Корпус ремонта и обслуживания'),
('Открытая стоянка', 'PARKING', NULL, 1, NULL, 'г. Город, ул. Промышленная, 3', 30, 'Стоянка для исправного транспорта'),
('Склад агрегатов', 'WAREHOUSE', NULL, 1, NULL, 'г. Город, ул. Промышленная, 4', 100, 'Склад запчастей и агрегатов');

INSERT INTO vehicle (
    inventory_number, registration_number, vin, category_id, brand_name, model_name,
    manufacture_year, purchase_date, commissioning_date, current_mileage, status,
    passenger_capacity, load_capacity_kg, cargo_volume_m3, body_type, taxi_license_number,
    service_purpose, color, engine_number, chassis_number, notes
) VALUES
('INV-001', 'А111АА77', 'VINBUS000000000001', 1, 'ЛиАЗ', '5292', 2018, '2018-03-10', '2018-03-20', 0, 'ACTIVE', 100, NULL, NULL, NULL, NULL, NULL, 'Белый', 'ENG-BUS-001', 'CHS-BUS-001', 'Городской автобус'),
('INV-002', 'А222АА77', 'VINMINI00000000002', 2, 'ГАЗ', 'Газель Next', 2020, '2020-05-01', '2020-05-10', 0, 'ACTIVE', 18, NULL, NULL, NULL, NULL, NULL, 'Жёлтый', 'ENG-MINI-002', 'CHS-MINI-002', 'Маршрутное такси'),
('INV-003', 'А333АА77', 'VINTAXI00000000003', 3, 'Skoda', 'Octavia', 2021, '2021-06-01', '2021-06-15', 0, 'ACTIVE', 4, NULL, NULL, 'SEDAN', 'TX-1001', NULL, 'Белый', 'ENG-TAXI-003', 'CHS-TAXI-003', 'Городское такси'),
('INV-004', 'А444АА77', 'VINCAR000000000004', 4, 'Toyota', 'Camry', 2019, '2019-04-10', '2019-04-20', 0, 'ACTIVE', 5, NULL, NULL, 'SEDAN', NULL, NULL, 'Чёрный', 'ENG-CAR-004', 'CHS-CAR-004', 'Служебный легковой автомобиль'),
('INV-005', 'А555АА77', 'VINCARGO0000000005', 5, 'КАМАЗ', '65115', 2017, '2017-07-12', '2017-07-25', 0, 'ACTIVE', NULL, 15000, 20, 'DUMP', NULL, NULL, 'Оранжевый', 'ENG-CARGO-005', 'CHS-CARGO-005', 'Грузовой самосвал'),
('INV-006', 'А666АА77', 'VINSERV00000000006', 6, 'УАЗ', '3909', 2016, '2016-08-18', '2016-08-25', 0, 'ACTIVE', NULL, NULL, NULL, 'VAN', NULL, 'Техническое обслуживание маршрутов', 'Серый', 'ENG-SERV-006', 'CHS-SERV-006', 'Вспомогательный транспорт');

INSERT INTO vehicle_acquisition (vehicle_id, acquisition_date, acquisition_type, supplier_name, document_number, cost, notes) VALUES
(1, '2018-03-10', 'PURCHASE', 'ООО Автотехника', 'DOC-ACQ-001', 8500000, NULL),
(2, '2020-05-01', 'PURCHASE', 'ООО КомТранс', 'DOC-ACQ-002', 2300000, NULL),
(3, '2021-06-01', 'PURCHASE', 'ООО ГородТаксиПарк', 'DOC-ACQ-003', 1800000, NULL),
(4, '2019-04-10', 'PURCHASE', 'ООО АвтоСервисПоставка', 'DOC-ACQ-004', 2100000, NULL),
(5, '2017-07-12', 'PURCHASE', 'ПАО КАМАЗ-Поставка', 'DOC-ACQ-005', 5400000, NULL),
(6, '2016-08-18', 'PURCHASE', 'ООО СпецАвто', 'DOC-ACQ-006', 1200000, NULL);

INSERT INTO vehicle_location_history (vehicle_id, garage_object_id, start_date, end_date, notes) VALUES
(1, 4, '2024-01-01', NULL, 'Автобус размещён на открытой стоянке'),
(2, 4, '2024-01-01', NULL, 'Маршрутка размещена на открытой стоянке'),
(3, 1, '2024-01-01', NULL, 'Такси размещено в главном гараже'),
(4, 1, '2024-01-01', NULL, 'Служебный автомобиль размещён в главном гараже'),
(5, 4, '2024-01-01', NULL, 'Грузовой транспорт на стоянке'),
(6, 1, '2024-01-01', NULL, 'Вспомогательный транспорт в гараже');

INSERT INTO vehicle_driver_assignment (vehicle_id, driver_employee_id, start_date, end_date, assignment_type, notes) VALUES
(1, 4, '2024-01-01', NULL, 'PRIMARY', 'Основной водитель автобуса'),
(2, 5, '2024-01-01', NULL, 'PRIMARY', 'Основной водитель маршрутки'),
(5, 6, '2024-01-01', NULL, 'PRIMARY', 'Основной водитель грузового автомобиля');

INSERT INTO route (route_number, name, route_type, start_point, end_point, length_km, is_active, notes) VALUES
('10', 'Маршрут №10 Центральный район - Вокзал', 'BUS', 'Центральный район', 'ЖД Вокзал', 15.5, TRUE, NULL),
('25', 'Маршрут №25 Автовокзал - Южный район', 'MINIBUS', 'Автовокзал', 'Южный район', 11.2, TRUE, NULL);

INSERT INTO route_vehicle_assignment (route_id, vehicle_id, start_date, end_date, shift_info, notes) VALUES
(1, 1, '2024-01-01', NULL, 'Утренняя и дневная смена', 'Автобус закреплён за маршрутом №10'),
(2, 2, '2024-01-01', NULL, 'Полный день', 'Маршрутка закреплена за маршрутом №25');

INSERT INTO transportation_record (
    vehicle_id, route_id, record_type, record_date, mileage_km, hours_used,
    passenger_count, cargo_weight_kg, cargo_volume_m3, trip_count, revenue, description, notes
) VALUES
(1, 1, 'PASSENGER', '2026-04-20', 180, 10, 920, NULL, NULL, 12, 18500, 'Работа автобуса на маршруте №10', NULL),
(1, 1, 'PASSENGER', '2026-04-21', 176, 10, 870, NULL, NULL, 12, 17800, 'Работа автобуса на маршруте №10', NULL),
(2, 2, 'PASSENGER', '2026-04-20', 140, 9, 410, NULL, NULL, 15, 9200, 'Работа маршрутки на маршруте №25', NULL),
(5, NULL, 'CARGO', '2026-04-20', 95, 7, NULL, 12000, 16, 3, NULL, 'Перевозка строительных материалов', NULL),
(6, NULL, 'SERVICE', '2026-04-20', 48, 6, NULL, NULL, NULL, 2, NULL, 'Выезд для технического сопровождения', NULL);

INSERT INTO repair (
    vehicle_id, repair_type_id, workshop_id, section_id, brigade_id,
    start_date, end_date, reason, description, total_cost, status, notes
) VALUES
(5, 1, 1, 1, 1, '2026-04-10', '2026-04-12', 'Плановый текущий ремонт', 'Замена тормозных элементов и диагностика двигателя', 0, 'COMPLETED', NULL),
(1, 4, 1, 1, 1, '2026-04-15', '2026-04-15', 'Профилактический осмотр', 'Плановое техническое обслуживание автобуса', 0, 'COMPLETED', NULL);

INSERT INTO repair_work (
    repair_id, employee_id, work_type, description, quantity, cost, completed_at, notes
) VALUES
(1, 8, 'Замена тормозных колодок', 'Замена передних и задних тормозных колодок', 1, 12000, '2026-04-11 12:00:00', NULL),
(1, 7, 'Сварочные работы', 'Устранение трещины крепления', 1, 8000, '2026-04-11 15:30:00', NULL),
(2, 8, 'Диагностика', 'Плановая диагностика автобуса', 1, 5000, '2026-04-15 11:00:00', NULL);

INSERT INTO component (
    component_type, serial_number, model, manufacturer, production_date, purchase_date, status, notes
) VALUES
('ENGINE', 'ENG-COMP-001', 'КАМАЗ-740', 'КАМАЗ', '2020-01-10', '2020-02-01', 'INSTALLED', 'Двигатель для грузового автомобиля'),
('GEARBOX', 'GBX-COMP-002', 'КПП-Model-X', 'АвтоМех', '2021-03-11', '2021-04-01', 'IN_STOCK', 'Коробка передач на складе'),
('BRAKE_SYSTEM', 'BRK-COMP-003', 'Brake-Pro', 'ТехМаш', '2025-01-01', '2025-02-01', 'IN_STOCK', 'Комплект тормозной системы');

INSERT INTO vehicle_component_history (
    vehicle_id, component_id, repair_id, action_type, action_date, cost, notes
) VALUES
(5, 1, NULL, 'INSTALLED', '2025-01-10', 0, 'Установка двигателя на грузовой автомобиль'),
(5, 3, 1, 'INSTALLED', '2026-04-11', 15000, 'Установка нового тормозного комплекта'),
(5, 3, 1, 'REPAIRED', '2026-04-11', 3000, 'Дополнительная настройка тормозной системы');

INSERT INTO app_user (username, password_hash, employee_id, is_active, created_at, last_login_at) VALUES
('superadmin', '$2a$10$exampleSuperAdminHash', NULL, TRUE, NOW(), NOW()),
('dispatcher1', '$2a$10$exampleDispatcherHash', 9, TRUE, NOW(), NOW()),
('mechanic1', '$2a$10$exampleMechanicHash', 8, TRUE, NOW(), NOW()),
('hr1', '$2a$10$exampleHrHash', 10, TRUE, NOW(), NOW()),
('viewer1', '$2a$10$exampleViewerHash', NULL, TRUE, NOW(), NOW());

INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 3),
(3, 5),
(4, 4),
(5, 6);
