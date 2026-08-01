INSERT IGNORE INTO ticket_category
(name, code, description, status, created_at)
VALUES
(
    'Login Access Issue',
    'LOGIN_ACCESS_ISSUE',
    'Login, account lock and authentication issues',
    'ACTIVE',
    NOW()
),
(
    'Application Technical Issue',
    'APP_TECHNICAL_ISSUE',
    'Application crash, loading and performance issues',
    'ACTIVE',
    NOW()
),
(
    'Registration and Identity Verification Issue',
    'REGISTRATION_EKYC_ISSUE',
    'Registration, document and identity verification issues',
    'ACTIVE',
    NOW()
),
(
    'Verification Code and Notification Issue',
    'OTP_NOTIFICATION_ISSUE',
    'Verification code, SMS, email and alert issues',
    'ACTIVE',
    NOW()
),
(
    'Own Bank Transfer Issue',
    'OWN_BANK_TRANSFER_ISSUE',
    'Transfer issues between accounts in the same bank',
    'ACTIVE',
    NOW()
),
(
    'Other Bank Transfer Issue',
    'OTHER_BANK_TRANSFER_ISSUE',
    'Transfer issues involving another bank',
    'ACTIVE',
    NOW()
),
(
    'Bill Payment Issue',
    'BILL_PAYMENT_ISSUE',
    'Utility and other bill-payment issues',
    'ACTIVE',
    NOW()
),
(
    'Card Management Issue',
    'CARD_MANAGEMENT_ISSUE',
    'Card activation, blocking, linking and management issues',
    'ACTIVE',
    NOW()
),
(
    'Card Transaction Issue',
    'CARD_TRANSACTION_ISSUE',
    'Card payment, withdrawal, reversal and duplicate-charge issues',
    'ACTIVE',
    NOW()
),
(
    'Account Balance and Statement Issue',
    'ACCOUNT_BALANCE_STATEMENT_ISSUE',
    'Account balance, transaction history and statement issues',
    'ACTIVE',
    NOW()
),
(
    'Beneficiary Issue',
    'BENEFICIARY_ISSUE',
    'Beneficiary creation, update and activation issues',
    'ACTIVE',
    NOW()
),
(
    'Profile and Security Issue',
    'PROFILE_SECURITY_ISSUE',
    'Profile security, unknown access and device issues',
    'ACTIVE',
    NOW()
),
(
    'General Inquiry',
    'GENERAL_INQUIRY',
    'General customer-support questions',
    'ACTIVE',
    NOW()
);