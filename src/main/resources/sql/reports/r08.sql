SELECT vc.name AS "Категория",
       SUM(tr.mileage_km) AS "Суммарный пробег, км"
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY vc.name;
