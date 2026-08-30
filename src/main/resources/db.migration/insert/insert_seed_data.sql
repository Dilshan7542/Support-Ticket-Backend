INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'New', 'NEW', 'Ticket has been created', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'NEW');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Assigned', 'ASSIGNED', 'Ticket has been assigned', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'ASSIGNED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'In Progress', 'IN_PROGRESS', 'Ticket is being handled', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'IN_PROGRESS');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Waiting For Customer', 'WAITING_FOR_CUSTOMER', 'Waiting for customer response', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'WAITING_FOR_CUSTOMER');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Resolved', 'RESOLVED', 'Ticket has been resolved', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'RESOLVED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Closed', 'CLOSED', 'Ticket has been closed', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'CLOSED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Reopened', 'REOPENED', 'Ticket has been reopened', 'ACTIVE', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'REOPENED');



INSERT IGNORE INTO vendor
(code, name, description, status)
VALUES
(
    'DIGITAL_BANKING_VENDOR',
    'Digital Banking Solution Provider',
    'Handles digital banking application services',
    'ACTIVE'
),
(
    'EKYC_VENDOR',
    'Identity Verification Service Provider',
    'Handles registration and identity verification services',
    'ACTIVE'
),
(
    'NOTIFICATION_VENDOR',
    'Notification Service Provider',
    'Handles SMS, email and verification-code services',
    'ACTIVE'
),
(
    'PAYMENT_SERVICE_VENDOR',
    'Payment Service Provider',
    'Handles transfer and bill-payment services',
    'ACTIVE'
),
(
    'CARD_MANAGEMENT_VENDOR',
    'Card Management Service Provider',
    'Handles card activation, linking and card controls',
    'ACTIVE'
),
(
    'CARD_PROCESSING_VENDOR',
    'Card Transaction Service Provider',
    'Handles card payments, withdrawals and reversals',
    'ACTIVE'
),
(
    'CORE_BANKING_VENDOR',
    'Core Banking Service Provider',
    'Handles account, balance and statement services',
    'ACTIVE'
),
(
    'SECURITY_VENDOR',
    'Security Service Provider',
    'Handles account security and suspicious-access issues',
    'ACTIVE'
),
(
    'INTERNAL_SUPPORT',
    'Internal Customer Support',
    'Handles general customer support requests',
    'ACTIVE'
);