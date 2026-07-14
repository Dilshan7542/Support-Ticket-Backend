INSERT INTO ticket_category (name, code, description, status, created_at)
SELECT 'Login Issue', 'LOGIN_ISSUE', 'Login, password, account access, or permission issue', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'LOGIN_ISSUE');

INSERT INTO ticket_category (name, code, description, status, created_at)
SELECT 'Payment Issue', 'PAYMENT_ISSUE', 'Payment, invoice, refund, card, or billing related issue', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'PAYMENT_ISSUE');

INSERT INTO ticket_category (name, code, description, status, created_at)
SELECT 'Technical Issue', 'TECHNICAL_ISSUE', 'Application, device, network, error, or technical issue', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'TECHNICAL_ISSUE');

INSERT INTO ticket_category (name, code, description, status, created_at)
SELECT 'Performance Issue', 'PERFORMANCE_ISSUE', 'Slow response, timeout, delay, or performance issue', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'PERFORMANCE_ISSUE');

INSERT INTO ticket_category (name, code, description, status, created_at)
SELECT 'General Inquiry', 'GENERAL_INQUIRY', 'General question or support request', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'GENERAL_INQUIRY');

INSERT INTO ticket_category_department_mapping (company_id, category_id, department_id, status, created_at)
SELECT c.id, tc.id, d.id, 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
JOIN ticket_category tc ON tc.code = 'PAYMENT_ISSUE'
JOIN department d ON d.company_id = c.id AND d.code = 'PAYMENT_SUPPORT'
WHERE c.code = 'ABC_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_category_department_mapping m
      WHERE m.company_id = c.id AND m.category_id = tc.id
  );

INSERT INTO ticket_category_department_mapping (company_id, category_id, department_id, status, created_at)
SELECT c.id, tc.id, d.id, 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
JOIN ticket_category tc ON tc.code = 'PAYMENT_ISSUE'
JOIN department d ON d.company_id = c.id AND d.code = 'FINANCE'
WHERE c.code = 'XYZ_COMPANY'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_category_department_mapping m
      WHERE m.company_id = c.id AND m.category_id = tc.id
  );

INSERT INTO ticket_category_department_mapping (company_id, category_id, department_id, status, created_at)
SELECT c.id, tc.id, d.id, 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
JOIN ticket_category tc ON tc.code IN ('TECHNICAL_ISSUE', 'PERFORMANCE_ISSUE')
JOIN department d ON d.company_id = c.id AND d.code = 'TECHNICAL_SUPPORT'
WHERE c.code = 'ABC_BANK'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_category_department_mapping m
      WHERE m.company_id = c.id AND m.category_id = tc.id
  );

INSERT INTO ticket_category_department_mapping (company_id, category_id, department_id, status, created_at)
SELECT c.id, tc.id, d.id, 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
JOIN ticket_category tc ON tc.code IN ('TECHNICAL_ISSUE', 'PERFORMANCE_ISSUE')
JOIN department d ON d.company_id = c.id AND d.code = 'IT_SUPPORT'
WHERE c.code = 'XYZ_COMPANY'
  AND NOT EXISTS (
      SELECT 1 FROM ticket_category_department_mapping m
      WHERE m.company_id = c.id AND m.category_id = tc.id
  );

INSERT INTO ticket_category_department_mapping (company_id, category_id, department_id, status, created_at)
SELECT c.id, tc.id, d.id, 'ACTIVE', CURRENT_TIMESTAMP
FROM company c
JOIN ticket_category tc ON tc.code IN ('LOGIN_ISSUE', 'GENERAL_INQUIRY')
JOIN department d ON d.company_id = c.id AND d.code = 'CUSTOMER_CARE'
WHERE c.code IN ('ABC_BANK', 'XYZ_COMPANY')
  AND NOT EXISTS (
      SELECT 1 FROM ticket_category_department_mapping m
      WHERE m.company_id = c.id AND m.category_id = tc.id
  );
