SELECT COUNT(DISTINCT driver_employee_id) AS "Число текущих водителей"
FROM vehicle_driver_assignment
WHERE vehicle_id = :vehicle_id
  AND end_date IS NULL;
