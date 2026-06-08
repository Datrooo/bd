SELECT b.id AS "ID бригады",
       b.name AS "Бригада",
       COUNT(DISTINCT r.id) AS "Число ремонтов",
       COUNT(rw.id) AS "Число выполненных работ",
       COALESCE(SUM(rw.cost), 0) AS "Общая стоимость работ"
FROM brigade b
LEFT JOIN repair r ON r.brigade_id = b.id
LEFT JOIN repair_work rw ON rw.repair_id = r.id
GROUP BY b.id, b.name
ORDER BY b.name;
