SELECT r.id AS route_id, r.route_number, r.name,
       COUNT(DISTINCT rva.vehicle_id) AS vehicles_count,
       COALESCE(SUM(tr.passenger_count), 0) AS total_passengers,
       COALESCE(SUM(tr.revenue), 0) AS total_revenue
FROM route r
LEFT JOIN route_vehicle_assignment rva ON rva.route_id = r.id
LEFT JOIN transportation_record tr
       ON tr.route_id = r.id
      AND tr.record_type = 'PASSENGER'
      AND tr.record_date BETWEEN :start_date AND :end_date
GROUP BY r.id, r.route_number, r.name
ORDER BY r.route_number;
