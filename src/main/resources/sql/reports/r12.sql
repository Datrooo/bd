SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       COUNT(r.id) AS "Число ремонтов",
       COALESCE(SUM(r.total_cost), 0) AS "Общая стоимость ремонтов"
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.id = :vehicle_id
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY v.id, v.inventory_number, v.registration_number;
