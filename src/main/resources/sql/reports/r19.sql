SELECT v.brand_name AS "Марка транспорта",
       c.component_type AS "Тип агрегата",
       COUNT(vch.id) AS "Число установленных агрегатов",
       COALESCE(SUM(vch.cost), 0) AS "Общая стоимость агрегатов"
FROM vehicle_component_history vch
JOIN vehicle v ON v.id = vch.vehicle_id
JOIN component c ON c.id = vch.component_id
WHERE v.brand_name = :brand_name
  AND c.component_type = :component_type
  AND vch.repair_id IS NOT NULL
  AND vch.action_type = 'INSTALLED'
  AND vch.action_date BETWEEN :start_date AND :end_date
GROUP BY v.brand_name, c.component_type
ORDER BY c.component_type;
