SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, go.name AS garage_object, go.object_type,
       w.name AS workshop_name, s.name AS section_name,
       vlh.start_date, vlh.end_date
FROM vehicle_location_history vlh
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN garage_object go ON go.id = vlh.garage_object_id
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
ORDER BY vc.name, v.inventory_number;
