SELECT r.id AS route_id, r.route_number, r.name AS route_name, r.route_type,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS vehicle_category, v.brand_name, v.model_name,
       rva.start_date, rva.end_date, rva.shift_info
FROM route_vehicle_assignment rva
JOIN route r ON r.id = rva.route_id
JOIN vehicle v ON v.id = rva.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE r.route_type IN ('BUS', 'MINIBUS')
  AND rva.end_date IS NULL
ORDER BY r.route_number, v.inventory_number;
