SELECT go.id, go.name, go.object_type, go.address, go.capacity,
       w.name AS workshop_name, s.name AS section_name
FROM garage_object go
LEFT JOIN workshop w ON w.id = go.workshop_id
LEFT JOIN section s ON s.id = go.section_id
ORDER BY go.object_type, go.name;
