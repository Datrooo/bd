SELECT e.id AS "ID сотрудника",
       e.last_name AS "Фамилия",
       e.first_name AS "Имя",
       e.middle_name AS "Отчество",
       rw.id AS "ID работы",
       rw.work_type AS "Вид работы",
       rw.description AS "Описание работы",
       rw.quantity AS "Количество",
       rw.cost AS "Стоимость",
       rw.completed_at AS "Дата выполнения",
       v.id AS "ID транспорта",
       v.inventory_number AS "Инвентарный номер",
       v.registration_number AS "Регистрационный номер"
FROM repair_work rw
JOIN employee e ON e.id = rw.employee_id
JOIN repair r ON r.id = rw.repair_id
JOIN vehicle v ON v.id = r.vehicle_id
WHERE rw.employee_id = :employee_id
  AND v.id = :vehicle_id
  AND rw.completed_at::date BETWEEN :start_date AND :end_date
ORDER BY rw.completed_at;
