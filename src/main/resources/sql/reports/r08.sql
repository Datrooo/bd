SELECT vc.name AS category, SUM(tr.mileage_km) AS total_mileage
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY vc.name;
