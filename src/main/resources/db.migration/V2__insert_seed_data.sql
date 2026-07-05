INSERT INTO department (name, description, status, created_at)
SELECT 'Technical Support', 'Technical issue handling department', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Technical Support');

INSERT INTO department (name, description, status, created_at)
SELECT 'Customer Care', 'General customer support department', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Customer Care');
