SELECT COUNT(DISTINCT driver_employee_id) AS total_drivers_for_vehicle
FROM vehicle_driver_assignment
WHERE vehicle_id = :vehicle_id
  AND end_date IS NULL;
