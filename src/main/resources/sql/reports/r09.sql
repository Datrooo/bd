SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       v.brand_name, v.model_name, SUM(tr.mileage_km) AS total_mileage
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number, v.brand_name, v.model_name;
