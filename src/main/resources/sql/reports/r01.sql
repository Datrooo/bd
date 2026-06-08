SELECT v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория",
       v.brand_name AS "Марка",
       v.model_name AS "Модель",
       v.manufacture_year AS "Год выпуска",
       v.status AS "Статус",
       v.current_mileage AS "Текущий пробег",
       v.passenger_capacity AS "Пассажировместимость",
       v.load_capacity_kg AS "Грузоподъемность, кг",
       v.cargo_volume_m3 AS "Объем груза, м³",
       v.body_type AS "Тип кузова",
       v.taxi_license_number AS "Номер лицензии такси",
       v.service_purpose AS "Служебное назначение"
FROM vehicle v
JOIN vehicle_category vc ON vc.id = v.category_id
ORDER BY vc.name, v.brand_name, v.model_name;
