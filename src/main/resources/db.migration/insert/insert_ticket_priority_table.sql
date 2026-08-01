

INSERT INTO ticket_priority (name, code, level, description, status, created_at)
SELECT 'Low', 'LOW', 1, 'Low priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'LOW');

INSERT INTO ticket_priority (name, code, level, description, status, created_at)
SELECT 'Medium', 'MEDIUM', 2, 'Medium priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'MEDIUM');

INSERT INTO ticket_priority (name, code, level, description, status, created_at)
SELECT 'High', 'HIGH', 3, 'High priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'HIGH');

INSERT INTO ticket_priority (name, code, level, description, status, created_at)
SELECT 'Critical', 'CRITICAL', 4, 'Critical priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'CRITICAL');
