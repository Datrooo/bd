SELECT b.id AS brigade_id, b.name AS brigade_name,
       e.id AS employee_id, e.last_name, e.first_name, e.middle_name,
       e.position, e.qualification
FROM brigade b
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE b.brigadier_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY e.last_name, e.first_name;
