SELECT e.id, e.personnel_number, e.last_name, e.first_name, e.middle_name,
       vda.assignment_type, vda.start_date, vda.end_date
FROM vehicle_driver_assignment vda
JOIN employee e ON e.id = vda.driver_employee_id
WHERE vda.vehicle_id = :vehicle_id
ORDER BY e.last_name, e.first_name;
