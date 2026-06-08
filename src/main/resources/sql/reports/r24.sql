SELECT s.id AS "ID участка",
       s.name AS "Участок",
       b.id AS "ID бригады",
       b.name AS "Бригада",
       e.id AS "ID сотрудника",
       e.last_name AS "Фамилия",
       e.first_name AS "Имя",
       e.middle_name AS "Отчество",
       e.position AS "Должность"
FROM section s
JOIN brigade b ON b.section_id = s.id
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE s.master_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY b.name, e.last_name, e.first_name;
