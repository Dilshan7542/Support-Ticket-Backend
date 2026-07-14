INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'Hardware Issue', 'HARDWARE_ISSUE', 'Computer, printer, device, or other hardware related issue', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Technical Support'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'HARDWARE_ISSUE');

INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'Software Issue', 'SOFTWARE_ISSUE', 'Application, installation, update, or software error issue', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Technical Support'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'SOFTWARE_ISSUE');

INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'Network Issue', 'NETWORK_ISSUE', 'Internet, Wi-Fi, VPN, or internal network connectivity issue', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Technical Support'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'NETWORK_ISSUE');

INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'Account/Login Issue', 'LOGIN_ISSUE', 'Login, password, account access, or permission issue', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Customer Care'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'LOGIN_ISSUE');

INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'Billing Issue', 'BILLING_ISSUE', 'Invoice, payment, subscription, or billing related issue', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Customer Care'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'BILLING_ISSUE');

INSERT INTO ticket_category (company_id, department_id, name, code, description, status, created_at)
SELECT NULL, d.id, 'General Inquiry', 'GENERAL_INQUIRY', 'General question or support request', 'ACTIVE', CURRENT_TIMESTAMP
FROM department d
WHERE d.name = 'Customer Care'
  AND NOT EXISTS (SELECT 1 FROM ticket_category WHERE code = 'GENERAL_INQUIRY');
