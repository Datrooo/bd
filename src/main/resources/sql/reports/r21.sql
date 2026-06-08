SELECT va.acquisition_date AS "Дата поступления",
       va.acquisition_type AS "Тип поступления",
       va.supplier_name AS "Поставщик",
       va.document_number AS "Номер документа",
       va.cost AS "Стоимость",
       v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория",
       v.brand_name AS "Марка",
       v.model_name AS "Модель"
FROM vehicle_acquisition va
JOIN vehicle v ON v.id = va.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE va.acquisition_date BETWEEN :start_date AND :end_date
ORDER BY va.acquisition_date, v.inventory_number;
