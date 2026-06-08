SELECT vc.name AS "Категория транспорта",
       go.id AS "ID гаражного объекта",
       go.name AS "Гаражный объект",
       go.object_type AS "Тип объекта",
       COUNT(DISTINCT v.id) AS "Число единиц транспорта"
FROM vehicle_location_history vlh
JOIN garage_object go ON go.id = vlh.garage_object_id
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vlh.end_date IS NULL
GROUP BY vc.name, go.id, go.name, go.object_type
ORDER BY vc.name, go.name;
