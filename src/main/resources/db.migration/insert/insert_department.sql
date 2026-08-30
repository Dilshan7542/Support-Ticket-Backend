INSERT IGNORE INTO department
(vendor_id, code, name, description, status)
VALUES
(
    (SELECT id FROM vendor
     WHERE code = 'DIGITAL_BANKING_VENDOR'),
    'DIGITAL_BANKING_SUPPORT',
    'Digital Banking Support',
    'Handles login and application technical issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'EKYC_VENDOR'),
    'ONBOARDING_EKYC_SUPPORT',
    'Onboarding and Identity Verification Support',
    'Handles registration and identity verification issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'NOTIFICATION_VENDOR'),
    'NOTIFICATION_SUPPORT',
    'Notification Support',
    'Handles verification code, SMS and email issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'DIGITAL_BANKING_VENDOR'),
    'INTERNAL_TRANSFER_SUPPORT',
    'Internal Transfer Support',
    'Handles same-bank transfers and beneficiary issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'PAYMENT_SERVICE_VENDOR'),
    'INTERBANK_TRANSFER_SUPPORT',
    'Interbank Transfer Support',
    'Handles transfers to other banks',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'PAYMENT_SERVICE_VENDOR'),
    'BILL_PAYMENT_SUPPORT',
    'Bill Payment Support',
    'Handles bill-payment issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'CARD_MANAGEMENT_VENDOR'),
    'CARD_MANAGEMENT_SUPPORT',
    'Card Management Support',
    'Handles card activation, blocking and linking issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'CARD_PROCESSING_VENDOR'),
    'CARD_TRANSACTION_SUPPORT',
    'Card Transaction Support',
    'Handles card payment and withdrawal issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'CORE_BANKING_VENDOR'),
    'ACCOUNT_SUPPORT',
    'Account Support',
    'Handles account balance and statement issues',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'SECURITY_VENDOR'),
    'SECURITY_SUPPORT',
    'Security Support',
    'Handles account security and suspicious access',
    'ACTIVE'
),
(
    (SELECT id FROM vendor
     WHERE code = 'INTERNAL_SUPPORT'),
    'CUSTOMER_SUPPORT',
    'Customer Support',
    'Handles general customer inquiries',
    'ACTIVE'
);