SELECT vc.name AS category,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY vc.name;
