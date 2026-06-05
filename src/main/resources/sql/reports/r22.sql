SELECT vd.disposal_date, vd.disposal_type, vd.reason, vd.document_number, vd.amount_received,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, v.brand_name, v.model_name
FROM vehicle_disposal vd
JOIN vehicle v ON v.id = vd.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vd.disposal_date BETWEEN :start_date AND :end_date
ORDER BY vd.disposal_date, v.inventory_number;
