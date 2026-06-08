SELECT
    CONCAT_WS(' ', e.last_name, e.first_name, e.middle_name) AS "Сотрудник",
    CASE UPPER(e.position)
        WHEN 'BRIGADIER' THEN 'Бригадир'
        WHEN 'WELDER' THEN 'Сварщик'
        WHEN 'LOCKSMITH' THEN 'Слесарь'
        WHEN 'DRIVER' THEN 'Водитель'
        WHEN 'MASTER' THEN 'Мастер'
        WHEN 'WORKSHOP_CHIEF' THEN 'Начальник цеха'
        ELSE e.position
    END AS "Должность",
    CONCAT_WS(' / ', w.name, s.name, b.name) AS "Подразделение",
    CASE
        WHEN e.id = b.brigadier_employee_id
            THEN COALESCE(NULLIF(CONCAT_WS(' ', mast.last_name, mast.first_name, mast.middle_name), ''), 'Не назначен')
        ELSE COALESCE(NULLIF(CONCAT_WS(' ', brig.last_name, brig.first_name, brig.middle_name), ''), 'Не назначен')
    END AS "Непосредственный руководитель",
    CASE
        WHEN e.id = b.brigadier_employee_id AND mast.id IS NOT NULL THEN 'Мастер'
        WHEN e.id <> b.brigadier_employee_id AND brig.id IS NOT NULL THEN 'Бригадир'
        ELSE 'Не назначен'
    END AS "Роль руководителя",
    CASE
        WHEN e.id = b.brigadier_employee_id THEN
            CONCAT(
                'Начальник цеха: ',
                COALESCE(NULLIF(CONCAT_WS(' ', chief.last_name, chief.first_name, chief.middle_name), ''), 'не назначен')
            )
        ELSE
            CONCAT(
                'Мастер: ',
                COALESCE(NULLIF(CONCAT_WS(' ', mast.last_name, mast.first_name, mast.middle_name), ''), 'не назначен'),
                ' -> Начальник цеха: ',
                COALESCE(NULLIF(CONCAT_WS(' ', chief.last_name, chief.first_name, chief.middle_name), ''), 'не назначен')
            )
    END AS "Далее по иерархии"
FROM employee_brigade_assignment eba
JOIN employee e ON e.id = eba.employee_id
JOIN brigade b ON b.id = eba.brigade_id
LEFT JOIN employee brig ON brig.id = b.brigadier_employee_id
JOIN section s ON s.id = b.section_id
LEFT JOIN employee mast ON mast.id = s.master_employee_id
JOIN workshop w ON w.id = s.workshop_id
LEFT JOIN employee chief ON chief.id = w.chief_employee_id
WHERE eba.end_date IS NULL
ORDER BY w.name, s.name, b.name, e.last_name, e.first_name;
