SELECT tr.id AS "ID перевозки",
       tr.record_date AS "Дата перевозки",
       v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       tr.mileage_km AS "Пробег, км",
       tr.hours_used AS "Часы работы",
       tr.cargo_weight_kg AS "Вес груза, кг",
       tr.cargo_volume_m3 AS "Объем груза, м³",
       tr.trip_count AS "Число поездок",
       tr.description AS "Описание"
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE tr.record_type = 'CARGO'
  AND v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
ORDER BY tr.record_date;
