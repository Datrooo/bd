SELECT r.id AS "ID маршрута",
       r.route_number AS "Номер маршрута",
       r.name AS "Название маршрута",
       r.route_type AS "Тип маршрута",
       v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория транспорта",
       v.brand_name AS "Марка",
       v.model_name AS "Модель",
       rva.start_date AS "Дата начала",
       rva.end_date AS "Дата окончания",
       rva.shift_info AS "Информация о смене"
FROM route_vehicle_assignment rva
JOIN route r ON r.id = rva.route_id
JOIN vehicle v ON v.id = rva.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE r.route_type IN ('BUS', 'MINIBUS')
  AND rva.end_date IS NULL
ORDER BY r.route_number, v.inventory_number;
