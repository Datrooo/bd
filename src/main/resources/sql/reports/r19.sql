SELECT v.brand_name,
       c.component_type,
       COUNT(vch.id) AS component_actions_count,
       COALESCE(SUM(vch.cost), 0) AS total_component_cost
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
