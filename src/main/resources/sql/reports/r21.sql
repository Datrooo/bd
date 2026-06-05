SELECT va.acquisition_date, va.acquisition_type, va.supplier_name, va.document_number, va.cost,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, v.brand_name, v.model_name
FROM vehicle_acquisition va
JOIN vehicle v ON v.id = va.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE va.acquisition_date BETWEEN :start_date AND :end_date
ORDER BY va.acquisition_date, v.inventory_number;
