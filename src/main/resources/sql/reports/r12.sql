SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.id = :vehicle_id
  AND r.start_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number;
