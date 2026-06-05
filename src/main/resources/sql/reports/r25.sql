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
