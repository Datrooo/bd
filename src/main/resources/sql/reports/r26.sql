SELECT e.id AS employee_id, e.last_name, e.first_name, e.middle_name,
       rw.id AS repair_work_id, rw.work_type, rw.description,
       rw.quantity, rw.cost, rw.completed_at,
       r.id AS repair_id, r.start_date, r.end_date,
       v.id AS vehicle_id, v.inventory_number, v.registration_number
FROM repair_work rw
JOIN employee e ON e.id = rw.employee_id
JOIN repair r ON r.id = rw.repair_id
JOIN vehicle v ON v.id = r.vehicle_id
WHERE rw.employee_id = :employee_id
  AND rw.completed_at::date BETWEEN :start_date AND :end_date
ORDER BY rw.completed_at;
