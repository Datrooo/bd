-- =========================================================
-- SQL-ЗАПРОСЫ ПО ТЗ
-- Используются параметры вида :vehicle_id, :start_date и т.д.
-- =========================================================

-- 1. Данные об автопарке предприятия
SELECT v.id, v.inventory_number, v.registration_number, vc.name AS category,
       v.brand_name, v.model_name, v.manufacture_year, v.status, v.current_mileage,
       v.passenger_capacity, v.load_capacity_kg, v.cargo_volume_m3,
       v.body_type, v.taxi_license_number, v.service_purpose
FROM vehicle v
JOIN vehicle_category vc ON vc.id = v.category_id
ORDER BY vc.name, v.brand_name, v.model_name;

-- 2. Перечень водителей по предприятию
SELECT e.id, e.personnel_number, e.last_name, e.first_name, e.middle_name,
       e.phone, e.status, e.qualification
FROM employee e
WHERE e.position = 'DRIVER'
ORDER BY e.last_name, e.first_name;

-- 3. Общее число водителей
SELECT COUNT(*) AS total_drivers
FROM employee
WHERE position = 'DRIVER';

-- 4. Водители по указанной автомашине
SELECT e.id, e.personnel_number, e.last_name, e.first_name, e.middle_name,
       vda.assignment_type, vda.start_date, vda.end_date
FROM vehicle_driver_assignment vda
JOIN employee e ON e.id = vda.driver_employee_id
WHERE vda.vehicle_id = :vehicle_id
ORDER BY e.last_name, e.first_name;

-- 5. Число водителей по указанной автомашине
SELECT COUNT(DISTINCT driver_employee_id) AS total_drivers_for_vehicle
FROM vehicle_driver_assignment
WHERE vehicle_id = :vehicle_id
  AND end_date IS NULL;

-- 6. Распределение водителей по автомобилям
SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS vehicle_category, v.brand_name, v.model_name,
       e.id AS driver_id, e.last_name, e.first_name, e.middle_name,
       vda.assignment_type, vda.start_date, vda.end_date
FROM vehicle_driver_assignment vda
JOIN vehicle v ON v.id = vda.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN employee e ON e.id = vda.driver_employee_id
ORDER BY v.id, e.last_name, e.first_name;

-- 7. Распределение пассажирского транспорта по маршрутам
SELECT r.id AS route_id, r.route_number, r.name AS route_name, r.route_type,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS vehicle_category, v.brand_name, v.model_name,
       rva.start_date, rva.end_date, rva.shift_info
FROM route_vehicle_assignment rva
JOIN route r ON r.id = rva.route_id
JOIN vehicle v ON v.id = rva.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE r.route_type IN ('BUS', 'MINIBUS')
  AND rva.end_date IS NULL
ORDER BY r.route_number, v.inventory_number;

-- 8. Пробег транспорта определенной категории за период
SELECT vc.name AS category, SUM(tr.mileage_km) AS total_mileage
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY vc.name;

-- 9. Пробег конкретной автомашины за период
SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       v.brand_name, v.model_name, SUM(tr.mileage_km) AS total_mileage
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number, v.brand_name, v.model_name;

-- 10. Число ремонтов и их стоимость для категории транспорта за период
SELECT vc.name AS category,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY vc.name;

-- 11. Число ремонтов и их стоимость для марки транспорта за период
SELECT v.brand_name,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.brand_name = :brand_name
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY v.brand_name;

-- 12. Число ремонтов и их стоимость для конкретной автомашины за период
SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       COUNT(r.id) AS repair_count,
       COALESCE(SUM(r.total_cost), 0) AS total_repair_cost
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.id = :vehicle_id
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY v.id, v.inventory_number, v.registration_number;

-- 13. Подчиненность персонала: рабочие -> бригадиры -> мастера -> начальники цехов
SELECT e.id AS employee_id,
       e.last_name || ' ' || e.first_name || COALESCE(' ' || e.middle_name, '') AS employee_fio,
       e.position AS employee_position,
       b.name AS brigade_name,
       brig.last_name || ' ' || brig.first_name || COALESCE(' ' || brig.middle_name, '') AS brigadier_fio,
       s.name AS section_name,
       mast.last_name || ' ' || mast.first_name || COALESCE(' ' || mast.middle_name, '') AS master_fio,
       w.name AS workshop_name,
       chief.last_name || ' ' || chief.first_name || COALESCE(' ' || chief.middle_name, '') AS workshop_chief_fio
FROM employee_brigade_assignment eba
JOIN employee e ON e.id = eba.employee_id
JOIN brigade b ON b.id = eba.brigade_id
LEFT JOIN employee brig ON brig.id = b.brigadier_employee_id
JOIN section s ON s.id = b.section_id
LEFT JOIN employee mast ON mast.id = s.master_employee_id
JOIN workshop w ON w.id = s.workshop_id
LEFT JOIN employee chief ON chief.id = w.chief_employee_id
WHERE eba.end_date IS NULL
ORDER BY workshop_name, section_name, brigade_name, employee_fio;

-- 14. Наличие гаражного хозяйства в целом
SELECT go.id, go.name, go.object_type, go.address, go.capacity,
       w.name AS workshop_name, s.name AS section_name
FROM garage_object go
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
ORDER BY go.object_type, go.name;

-- 15. Наличие гаражного хозяйства по каждой категории транспорта
SELECT vc.name AS category,
       go.id AS garage_object_id, go.name AS garage_object_name, go.object_type,
       COUNT(DISTINCT v.id) AS vehicles_count
FROM vehicle_location_history vlh
JOIN garage_object go ON go.id = vlh.garage_object_id
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vlh.end_date IS NULL
GROUP BY vc.name, go.id, go.name, go.object_type
ORDER BY vc.name, go.name;

-- 16. Распределение автотранспорта на предприятии
SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, go.name AS garage_object, go.object_type,
       w.name AS workshop_name, s.name AS section_name,
       vlh.start_date, vlh.end_date
FROM vehicle_location_history vlh
JOIN vehicle v ON v.id = vlh.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN garage_object go ON go.id = vlh.garage_object_id
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
WHERE vlh.end_date IS NULL
ORDER BY vc.name, v.inventory_number;

-- 17. Грузоперевозки, выполненные указанной автомашиной за период
SELECT tr.id, tr.record_date, v.id AS vehicle_id, v.inventory_number, v.registration_number,
       tr.mileage_km, tr.hours_used, tr.cargo_weight_kg, tr.cargo_volume_m3,
       tr.trip_count, tr.description
FROM transportation_record tr
JOIN vehicle v ON v.id = tr.vehicle_id
WHERE tr.record_type = 'CARGO'
  AND v.id = :vehicle_id
  AND tr.record_date BETWEEN :start_date AND :end_date
ORDER BY tr.record_date;

-- 18. Число использованных для ремонта агрегатов для категории транспорта за период
SELECT vc.name AS category,
       c.component_type,
       COUNT(vch.id) AS component_actions_count,
       COALESCE(SUM(vch.cost), 0) AS total_component_cost
FROM vehicle_component_history vch
JOIN vehicle v ON v.id = vch.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
JOIN component c ON c.id = vch.component_id
WHERE vc.name = :category_name
  AND c.component_type = :component_type
  AND vch.repair_id IS NOT NULL
  AND vch.action_type = 'INSTALLED'
  AND vch.action_date BETWEEN :start_date AND :end_date
GROUP BY vc.name, c.component_type
ORDER BY c.component_type;

-- 19. Число использованных для ремонта агрегатов для марки транспорта за период
SELECT v.brand_name,
       c.component_type,
       COUNT(vch.id) AS component_actions_count,
       COALESCE(SUM(vch.cost), 0) AS total_component_cost
FROM vehicle_component_history vch
JOIN vehicle v ON v.id = vch.vehicle_id
JOIN component c ON c.id = vch.component_id
WHERE v.brand_name = :brand_name
  AND c.component_type = :component_type
  AND vch.repair_id IS NOT NULL
  AND vch.action_type = 'INSTALLED'
  AND vch.action_date BETWEEN :start_date AND :end_date
GROUP BY v.brand_name, c.component_type
ORDER BY c.component_type;

-- 20. Число использованных для ремонта агрегатов для конкретной машины за период
SELECT v.id AS vehicle_id, v.inventory_number, v.registration_number,
       c.component_type,
       COUNT(vch.id) AS component_actions_count,
       COALESCE(SUM(vch.cost), 0) AS total_component_cost
FROM vehicle_component_history vch
JOIN vehicle v ON v.id = vch.vehicle_id
JOIN component c ON c.id = vch.component_id
WHERE v.id = :vehicle_id
  AND c.component_type = :component_type
  AND vch.repair_id IS NOT NULL
  AND vch.action_type = 'INSTALLED'
  AND vch.action_date BETWEEN :start_date AND :end_date
GROUP BY v.id, v.inventory_number, v.registration_number, c.component_type
ORDER BY c.component_type;

-- 21. Полученная техника за период
SELECT va.acquisition_date, va.acquisition_type, va.supplier_name, va.document_number, va.cost,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, v.brand_name, v.model_name
FROM vehicle_acquisition va
JOIN vehicle v ON v.id = va.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE va.acquisition_date BETWEEN :start_date AND :end_date
ORDER BY va.acquisition_date, v.inventory_number;

-- 22. Списанная техника за период
SELECT vd.disposal_date, vd.disposal_type, vd.reason, vd.document_number, vd.amount_received,
       v.id AS vehicle_id, v.inventory_number, v.registration_number,
       vc.name AS category, v.brand_name, v.model_name
FROM vehicle_disposal vd
JOIN vehicle v ON v.id = vd.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vd.disposal_date BETWEEN :start_date AND :end_date
ORDER BY vd.disposal_date, v.inventory_number;

-- 23. Состав подчиненных указанного бригадира
SELECT b.id AS brigade_id, b.name AS brigade_name,
       e.id AS employee_id, e.last_name, e.first_name, e.middle_name,
       e.position, e.qualification
FROM brigade b
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE b.brigadier_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY e.last_name, e.first_name;

-- 24. Состав подчиненных указанного мастера
SELECT s.id AS section_id, s.name AS section_name,
       b.id AS brigade_id, b.name AS brigade_name,
       e.id AS employee_id, e.last_name, e.first_name, e.middle_name, e.position
FROM section s
JOIN brigade b ON b.section_id = s.id
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE s.master_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY b.name, e.last_name, e.first_name;

-- 25. Состав подчиненных указанного начальника цеха
SELECT w.id AS workshop_id, w.name AS workshop_name,
       s.id AS section_id, s.name AS section_name,
       b.id AS brigade_id, b.name AS brigade_name,
       e.id AS employee_id, e.last_name, e.first_name, e.middle_name, e.position
FROM workshop w
JOIN section s ON s.workshop_id = w.id
JOIN brigade b ON b.section_id = s.id
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE w.chief_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY s.name, b.name, e.last_name, e.first_name;

-- 26. Работы, выполненные указанным специалистом за период в целом
SELECT e.id AS employee_id, e.last_name, e.first_name, e.middle_name,
       rw.id AS repair_work_id, rw.work_type, rw.description,
       rw.quantity, rw.cost, rw.completed_at,
       r.id AS repair_id, r.start_date, r.end_date,
       v.id AS vehicle_id, v.inventory_number, v.registration_number
FROM repair_work rw
JOIN employee e ON e.id = rw.employee_id
JOIN repair r ON r.id = rw.repair_id
JOIN vehicle v ON v.id = r.vehicle_id
WHERE rw.employee_id = :employee_id
  AND rw.completed_at::date BETWEEN :start_date AND :end_date
ORDER BY rw.completed_at;

-- 27. Работы, выполненные указанным специалистом за период по конкретной машине
SELECT e.id AS employee_id, e.last_name, e.first_name, e.middle_name,
       rw.id AS repair_work_id, rw.work_type, rw.description,
       rw.quantity, rw.cost, rw.completed_at,
       v.id AS vehicle_id, v.inventory_number, v.registration_number
FROM repair_work rw
JOIN employee e ON e.id = rw.employee_id
JOIN repair r ON r.id = rw.repair_id
JOIN vehicle v ON v.id = r.vehicle_id
WHERE rw.employee_id = :employee_id
  AND v.id = :vehicle_id
  AND rw.completed_at::date BETWEEN :start_date AND :end_date
ORDER BY rw.completed_at;

-- 28. Суммарная работа бригад по ремонту
SELECT b.id AS brigade_id, b.name AS brigade_name,
       COUNT(DISTINCT r.id) AS repairs_count,
       COUNT(rw.id) AS works_count,
       COALESCE(SUM(rw.cost), 0) AS total_work_cost
FROM brigade b
LEFT JOIN repair r ON r.brigade_id = b.id
LEFT JOIN repair_work rw ON rw.repair_id = r.id
GROUP BY b.id, b.name
ORDER BY b.name;
