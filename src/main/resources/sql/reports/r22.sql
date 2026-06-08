SELECT vd.disposal_date AS "Дата выбытия",
       vd.disposal_type AS "Тип выбытия",
       vd.reason AS "Причина",
       vd.document_number AS "Номер документа",
       vd.amount_received AS "Полученная сумма",
       v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер",
       vc.name AS "Категория",
       v.brand_name AS "Марка",
       v.model_name AS "Модель"
FROM vehicle_disposal vd
JOIN vehicle v ON v.id = vd.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vd.disposal_date BETWEEN :start_date AND :end_date
ORDER BY vd.disposal_date, v.inventory_number;
