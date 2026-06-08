SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория",
       go.name AS "Гаражный объект",
       go.object_type AS "Тип объекта",
       w.name AS "Цех",
       s.name AS "Участок",
       vlh.start_date AS "Дата размещения",
       vlh.end_date AS "Дата окончания размещения"
FROM vehicle_location_history vlh
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN garage_object go ON go.id = vlh.garage_object_id
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
WHERE vlh.end_date IS NULL
ORDER BY vc.name, v.inventory_number;
