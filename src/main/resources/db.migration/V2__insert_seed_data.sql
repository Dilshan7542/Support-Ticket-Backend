INSERT INTO company (name, code, description, status, created_at)
SELECT 'ABC Bank', 'ABC_BANK', 'Seed company for banking support examples', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM company WHERE code = 'ABC_BANK');

INSERT INTO company (name, code, description, status, created_at)
SELECT 'XYZ Company', 'XYZ_COMPANY', 'Seed company for general support examples', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM company WHERE code = 'XYZ_COMPANY');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'ABC Bank Payment Support', 'PAYMENT_SUPPORT', 'Payment issue handling department for ABC Bank', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'ABC_BANK'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'PAYMENT_SUPPORT');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'ABC Bank Technical Support', 'TECHNICAL_SUPPORT', 'Technical issue handling department for ABC Bank', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'ABC_BANK'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'TECHNICAL_SUPPORT');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'ABC Bank Customer Care', 'CUSTOMER_CARE', 'General customer support department for ABC Bank', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'ABC_BANK'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'CUSTOMER_CARE');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'XYZ Finance Department', 'FINANCE', 'Payment and billing issue handling department for XYZ Company', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'XYZ_COMPANY'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'FINANCE');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'XYZ IT Support', 'IT_SUPPORT', 'Technical issue handling department for XYZ Company', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'XYZ_COMPANY'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'IT_SUPPORT');

INSERT INTO department (company_id, name, code, description, status, created_at)
SELECT c.id, 'XYZ Customer Care', 'CUSTOMER_CARE', 'General customer support department for XYZ Company', 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
WHERE c.code = 'XYZ_COMPANY'
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.company_id = c.id AND d.code = 'CUSTOMER_CARE');
