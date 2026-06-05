SELECT e.id, e.personnel_number, e.last_name, e.first_name, e.middle_name,
       e.phone, e.status, e.qualification
FROM employee e
WHERE e.position = 'DRIVER'
ORDER BY e.last_name, e.first_name;
