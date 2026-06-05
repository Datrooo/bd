SELECT vc.name AS category,
       go.id AS garage_object_id, go.name AS garage_object_name, go.object_type,
       COUNT(DISTINCT v.id) AS vehicles_count
FROM vehicle_location_history vlh
JOIN garage_object go ON go.id = vlh.garage_object_id
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vlh.end_date IS NULL
GROUP BY vc.name, go.id, go.name, go.object_type
ORDER BY vc.name, go.name;
