SELECT tr.id, tr.record_date, v.id AS vehicle_id, v.inventory_number, v.registration_number,
       tr.mileage_km, tr.hours_used, tr.cargo_weight_kg, tr.cargo_volume_m3,
       tr.trip_count, tr.description
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE tr.record_type = 'CARGO'
  AND v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
ORDER BY tr.record_date;
