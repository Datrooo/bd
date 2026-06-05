SELECT b.id AS brigade_id, b.name AS brigade_name,
       COUNT(DISTINCT r.id) AS repairs_count,
       COUNT(rw.id) AS works_count,
       COALESCE(SUM(rw.cost), 0) AS total_work_cost
FROM brigade b
LEFT JOIN repair r ON r.brigade_id = b.id
LEFT JOIN repair_work rw ON rw.repair_id = r.id
GROUP BY b.id, b.name
ORDER BY b.name;
