SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория транспорта",
       v.brand_name AS "Марка",
       v.model_name AS "Модель",
       e.id AS "ID водителя",
       e.last_name AS "Фамилия водителя",
       e.first_name AS "Имя водителя",
       e.middle_name AS "Отчество водителя",
       vda.assignment_type AS "Тип назначения",
       vda.start_date AS "Дата начала",
       vda.end_date AS "Дата окончания"
FROM vehicle_driver_assignment vda
JOIN vehicle v ON v.id = vda.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN employee e ON e.id = vda.driver_employee_id
ORDER BY v.id, e.last_name, e.first_name;
