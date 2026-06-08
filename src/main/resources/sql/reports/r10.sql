SELECT vc.name AS "Категория",
       COUNT(r.id) AS "Число ремонтов",
       COALESCE(SUM(r.total_cost), 0) AS "Общая стоимость ремонтов"
FROM repair r
JOIN vehicle v ON v.id = r.vehicle_id
JOIN vehicle_category vc ON vc.id = v.category_id
WHERE vc.name = :category_name
  AND r.start_date <= :end_date
  AND (r.end_date IS NULL OR r.end_date >= :start_date)
GROUP BY vc.name;
