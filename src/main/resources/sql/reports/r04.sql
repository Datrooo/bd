SELECT e.id AS "ID сотрудника",
       e.personnel_number AS "Табельный номер",
       e.last_name AS "Фамилия",
       e.first_name AS "Имя",
       e.middle_name AS "Отчество",
       vda.assignment_type AS "Тип назначения",
       vda.start_date AS "Дата начала",
       vda.end_date AS "Дата окончания"
FROM vehicle_driver_assignment vda
JOIN employee e ON e.id = vda.driver_employee_id
WHERE vda.vehicle_id = :vehicle_id
ORDER BY e.last_name, e.first_name;
