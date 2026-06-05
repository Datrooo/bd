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
