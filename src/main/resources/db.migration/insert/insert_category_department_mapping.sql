INSERT IGNORE INTO ticket_category_department_mapping
(category_id, department_id, status)
VALUES
(
    (SELECT id FROM ticket_category
     WHERE code = 'LOGIN_ACCESS_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'DIGITAL_BANKING_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'APP_TECHNICAL_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'DIGITAL_BANKING_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'REGISTRATION_EKYC_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'ONBOARDING_EKYC_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'OTP_NOTIFICATION_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'NOTIFICATION_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'OWN_BANK_TRANSFER_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'INTERNAL_TRANSFER_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'OTHER_BANK_TRANSFER_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'INTERBANK_TRANSFER_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'BILL_PAYMENT_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'BILL_PAYMENT_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'CARD_MANAGEMENT_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'CARD_MANAGEMENT_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'CARD_TRANSACTION_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'CARD_TRANSACTION_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'ACCOUNT_BALANCE_STATEMENT_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'ACCOUNT_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'BENEFICIARY_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'INTERNAL_TRANSFER_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'PROFILE_SECURITY_ISSUE'),
    (SELECT id FROM department
     WHERE code = 'SECURITY_SUPPORT'),
    'ACTIVE'
),
(
    (SELECT id FROM ticket_category
     WHERE code = 'GENERAL_INQUIRY'),
    (SELECT id FROM department
     WHERE code = 'CUSTOMER_SUPPORT'),
    'ACTIVE'
);