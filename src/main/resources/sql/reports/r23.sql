SELECT b.id AS "ID бригады",
       b.name AS "Бригада",
       e.id AS "ID сотрудника",
       e.last_name AS "Фамилия",
       e.first_name AS "Имя",
       e.middle_name AS "Отчество",
       e.position AS "Должность",
       e.qualification AS "Квалификация"
FROM brigade b
JOIN employee_brigade_assignment eba ON eba.brigade_id = b.id
JOIN employee e ON e.id = eba.employee_id
WHERE b.brigadier_employee_id = :employee_id
  AND eba.end_date IS NULL
ORDER BY e.last_name, e.first_name;
