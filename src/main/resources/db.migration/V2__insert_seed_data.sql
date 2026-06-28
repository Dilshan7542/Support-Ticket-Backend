INSERT INTO departments (name, description, status, created_at)
SELECT 'Technical Support', 'Technical issue handling department', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Technical Support');

INSERT INTO departments (name, description, status, created_at)
SELECT 'Customer Care', 'General customer support department', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Customer Care');
