SELECT go.id AS "ID объекта",
       go.name AS "Название объекта",
       go.object_type AS "Тип объекта",
       go.address AS "Адрес",
       go.capacity AS "Вместимость",
       w.name AS "Цех",
       s.name AS "Участок"
FROM garage_object go
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
ORDER BY go.object_type, go.name;
