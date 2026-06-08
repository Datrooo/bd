SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       v.brand_name AS "Марка",
       v.model_name AS "Модель",
       SUM(tr.mileage_km) AS "Суммарный пробег, км"
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number, v.brand_name, v.model_name;
