SELECT e.id AS "ID сотрудника",
       e.personnel_number AS "Табельный номер",
       e.last_name AS "Фамилия",
       e.first_name AS "Имя",
       e.middle_name AS "Отчество",
       e.phone AS "Телефон",
       e.status AS "Статус",
       e.qualification AS "Квалификация"
FROM employee e
WHERE e.position = 'DRIVER'
ORDER BY e.last_name, e.first_name;
