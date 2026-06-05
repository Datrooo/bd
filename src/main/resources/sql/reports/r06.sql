SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS vehicle_category, v.brand_name, v.model_name,
       e.id AS driver_id, e.last_name, e.first_name, e.middle_name,
       vda.assignment_type, vda.start_date, vda.end_date
FROM vehicle_driver_assignment vda
JOIN vehicle v ON v.id = vda.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN employee e ON e.id = vda.driver_employee_id
ORDER BY v.id, e.last_name, e.first_name;
