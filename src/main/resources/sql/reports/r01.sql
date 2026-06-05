SELECT v.id, v.inventory_number, v.registration_number, vc.name AS category,
       v.brand_name, v.model_name, v.manufacture_year, v.status, v.current_mileage,
       v.passenger_capacity, v.load_capacity_kg, v.cargo_volume_m3,
       v.body_type, v.taxi_license_number, v.service_purpose
FROM vehicle v
JOIN vehicle_category vc ON vc.id = v.category_id
ORDER BY vc.name, v.brand_name, v.model_name;
