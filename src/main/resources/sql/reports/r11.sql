SELECT v.brand_name,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.brand_name = :brand_name
  AND r.start_date BETWEEN :start_date AND :end_date
GROUP BY v.brand_name;
