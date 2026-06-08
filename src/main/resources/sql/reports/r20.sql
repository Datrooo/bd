SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       c.component_type AS "Тип агрегата",
       COUNT(vch.id) AS "Число установленных агрегатов",
       COALESCE(SUM(vch.cost), 0) AS "Общая стоимость агрегатов"
FROM vehicle_component_history vch
JOIN vehicle v ON v.id = vch.vehicle_id
JOIN component c ON c.id = vch.component_id
WHERE v.id = :vehicle_id
  AND c.component_type = :component_type
  AND vch.repair_id IS NOT NULL
  AND vch.action_type = 'INSTALLED'
  AND vch.action_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number, c.component_type
ORDER BY c.component_type;
