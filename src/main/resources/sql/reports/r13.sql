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
