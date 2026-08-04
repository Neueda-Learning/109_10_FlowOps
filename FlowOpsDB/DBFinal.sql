create database salary_payment_db;
show databases;
use salary_payment_db;
show tables;
USE salary_payment_db;

select database();

-- =========================================================
-- 1) CURRENCY
-- =========================================================
INSERT INTO currency (currency_code, currency_name, currency_symbol, is_supported) VALUES
('INR', 'Indian Rupee', 'Rs', b'1'),
('USD', 'US Dollar', '$', b'1'),
('EUR', 'Euro', 'EUR', b'1'),
('GBP', 'British Pound', 'GBP', b'1'),
('AED', 'UAE Dirham', 'AED', b'1'),
('SGD', 'Singapore Dollar', 'SGD', b'1')
ON DUPLICATE KEY UPDATE
currency_name = VALUES(currency_name),
currency_symbol = VALUES(currency_symbol),
is_supported = VALUES(is_supported);

-- =========================================================
-- 2) BANK (20 = 10 India + 10 Offshore)
-- =========================================================
INSERT INTO bank (bank_name, country_code, created_at, is_active, swift_code) VALUES
-- India (10)
('State Bank of India',      'IND', NOW(6), b'1', 'SBININBBXXX'),
('HDFC Bank',                'IND', NOW(6), b'1', 'HDFCINBBXXX'),
('ICICI Bank',               'IND', NOW(6), b'1', 'ICICINBBXXX'),
('Axis Bank',                'IND', NOW(6), b'1', 'UTIBINBBXXX'),
('Punjab National Bank',     'IND', NOW(6), b'1', 'PUNBINBBXXX'),
('Bank of Baroda',           'IND', NOW(6), b'1', 'BARBINBBXXX'),
('Kotak Mahindra Bank',      'IND', NOW(6), b'1', 'KKBKINBBXXX'),
('IndusInd Bank',            'IND', NOW(6), b'1', 'INDBINBBXXX'),
('Yes Bank',                 'IND', NOW(6), b'1', 'YESBINBBXXX'),
('Canara Bank',              'IND', NOW(6), b'1', 'CNRBINBBXXX'),
-- Offshore (10)
('JPMorgan Chase Bank',      'USA', NOW(6), b'1', 'CHASUS33XXX'),
('Bank of America',          'USA', NOW(6), b'1', 'BOFAUS3NXXX'),
('HSBC UK',                  'GBR', NOW(6), b'1', 'MIDLGB22XXX'),
('Barclays Bank UK',         'GBR', NOW(6), b'1', 'BARCGB22XXX'),
('Deutsche Bank',            'DEU', NOW(6), b'1', 'DEUTDEFFXXX'),
('BNP Paribas',              'FRA', NOW(6), b'1', 'BNPAFRPPXXX'),
('DBS Bank',                 'SGP', NOW(6), b'1', 'DBSSSGSGXXX'),
('Emirates NBD',             'ARE', NOW(6), b'1', 'EBILAEADXXX'),
('ANZ Bank',                 'AUS', NOW(6), b'1', 'ANZBAU3MXXX'),
('MUFG Bank',                'JPN', NOW(6), b'1', 'BOTKJPJTXXX');

-- =========================================================
-- 3) ACCOUNT (20 = 10 India + 10 Offshore)
-- account_type: BUSINESS | PERSONAL
-- status: ACTIVE | BLOCKED
-- bank_id assumed 1..20 from fresh insert above
-- =========================================================
INSERT INTO account
(account_holder_name, account_number, account_type, balance, country_code, created_at, status, bank_id, currency_code)
VALUES
-- India (10)
('Aarav Sharma',      'IN00010001', 'PERSONAL', 250000.00, 'IND', NOW(6), 'ACTIVE', 1,  'INR'),
('Isha Patel',        'IN00010002', 'BUSINESS', 430000.00, 'IND', NOW(6), 'ACTIVE', 2,  'INR'),
('Rohan Mehta',       'IN00010003', 'PERSONAL', 125000.00, 'IND', NOW(6), 'ACTIVE', 3,  'INR'),
('Neha Verma',        'IN00010004', 'BUSINESS', 980000.00, 'IND', NOW(6), 'ACTIVE', 4,  'INR'),
('Karan Singh',       'IN00010005', 'PERSONAL', 510000.00, 'IND', NOW(6), 'ACTIVE', 5,  'INR'),
('Priya Nair',        'IN00010006', 'BUSINESS', 300000.00, 'IND', NOW(6), 'ACTIVE', 6,  'INR'),
('Rahul Kapoor',      'IN00010007', 'PERSONAL', 760000.00, 'IND', NOW(6), 'ACTIVE', 7,  'INR'),
('Sneha Reddy',       'IN00010008', 'BUSINESS', 220000.00, 'IND', NOW(6), 'ACTIVE', 8,  'INR'),
('Vikram Joshi',      'IN00010009', 'PERSONAL', 890000.00, 'IND', NOW(6), 'ACTIVE', 9,  'INR'),
('Ananya Das',        'IN00010010', 'BUSINESS', 150000.00, 'IND', NOW(6), 'BLOCKED',10, 'INR'),

-- Offshore (10)
('Michael Brown',     'US00020001', 'PERSONAL', 12500.00,  'USA', NOW(6), 'ACTIVE', 11, 'USD'),
('Emma Wilson',       'US00020002', 'BUSINESS', 9800.00,   'USA', NOW(6), 'ACTIVE', 12, 'USD'),
('Oliver Smith',      'GB00020003', 'PERSONAL', 7300.00,   'GBR', NOW(6), 'ACTIVE', 13, 'GBP'),
('Sophia Taylor',     'GB00020004', 'BUSINESS', 15400.00,  'GBR', NOW(6), 'ACTIVE', 14, 'GBP'),
('Lukas Schneider',   'DE00020005', 'PERSONAL', 11900.00,  'DEU', NOW(6), 'ACTIVE', 15, 'EUR'),
('Camille Martin',    'FR00020006', 'BUSINESS', 8600.00,   'FRA', NOW(6), 'ACTIVE', 16, 'EUR'),
('Ethan Lee',         'SG00020007', 'PERSONAL', 21000.00,  'SGP', NOW(6), 'ACTIVE', 17, 'SGD'),
('Noor Al Mansoori',  'AE00020008', 'BUSINESS', 34000.00,  'ARE', NOW(6), 'ACTIVE', 18, 'AED'),
('Liam Anderson',     'AU00020009', 'PERSONAL', 13200.00,  'AUS', NOW(6), 'ACTIVE', 19, 'USD'),
('Haruto Tanaka',     'JP00020010', 'BUSINESS', 185000.00, 'JPN', NOW(6), 'ACTIVE', 20, 'USD');

-- =========================================================
-- 4) RECEIVER (20 records mapped to account_id 1..20)
-- employment_status: ACTIVE | ON_LEAVE | SUSPENDED | TERMINATED
-- =========================================================
INSERT INTO receiver
(created_at, department, email, employee_id, employment_status, full_name, account_id)
VALUES
(NOW(6), 'Finance',   'aarav.r@corp.in',    'EMP1001', 'ACTIVE',    'Aarav Rao',        1),
(NOW(6), 'HR',        'isha.p@corp.in',     'EMP1002', 'ACTIVE',    'Isha Pradhan',     2),
(NOW(6), 'IT',        'rohan.m@corp.in',    'EMP1003', 'ON_LEAVE',  'Rohan Malhotra',   3),
(NOW(6), 'Ops',       'neha.v@corp.in',     'EMP1004', 'ACTIVE',    'Neha Venkat',      4),
(NOW(6), 'Sales',     'karan.s@corp.in',    'EMP1005', 'SUSPENDED', 'Karan Sethi',      5),
(NOW(6), 'Legal',     'priya.n@corp.in',    'EMP1006', 'ACTIVE',    'Priya Nambiar',    6),
(NOW(6), 'Admin',     'rahul.k@corp.in',    'EMP1007', 'ACTIVE',    'Rahul Khanna',     7),
(NOW(6), 'Procure',   'sneha.r@corp.in',    'EMP1008', 'TERMINATED','Sneha Rao',        8),
(NOW(6), 'Finance',   'vikram.j@corp.in',   'EMP1009', 'ACTIVE',    'Vikram Jain',      9),
(NOW(6), 'Security',  'ananya.d@corp.in',   'EMP1010', 'ACTIVE',    'Ananya Dutta',     10),

(NOW(6), 'Finance',   'michael.b@corp.us',  'EMP2001', 'ACTIVE',    'Michael Blake',    11),
(NOW(6), 'HR',        'emma.w@corp.us',     'EMP2002', 'ON_LEAVE',  'Emma White',       12),
(NOW(6), 'IT',        'oliver.s@corp.uk',   'EMP2003', 'ACTIVE',    'Oliver Stone',     13),
(NOW(6), 'Ops',       'sophia.t@corp.uk',   'EMP2004', 'ACTIVE',    'Sophia Turner',    14),
(NOW(6), 'Sales',     'lukas.s@corp.de',    'EMP2005', 'ACTIVE',    'Lukas Stein',      15),
(NOW(6), 'Legal',     'camille.m@corp.fr',  'EMP2006', 'SUSPENDED', 'Camille Moreau',   16),
(NOW(6), 'Admin',     'ethan.l@corp.sg',    'EMP2007', 'ACTIVE',    'Ethan Lim',        17),
(NOW(6), 'Procure',   'noor.m@corp.ae',     'EMP2008', 'ACTIVE',    'Noor Mansoor',     18),
(NOW(6), 'Finance',   'liam.a@corp.au',     'EMP2009', 'TERMINATED','Liam Archer',      19),
(NOW(6), 'Security',  'haruto.t@corp.jp',   'EMP2010', 'ACTIVE',    'Haruto Takeda',    20);

-- =========================================================
-- 5) PAYMENT (20 records)
-- status: COMPLETED | CREATED | FAILED | SENT | VALIDATED
-- receiver_id and sender_account_id assumed 1..20
-- =========================================================
INSERT INTO payment
(amount, created_at, error_code, error_message, idempotency_key, payment_type, reference, scheduled, status, updated_at, currency_code, receiver_id, sender_account_id)
VALUES
(15000.00, NOW(6), NULL, NULL, 'IDEMP-0001', 'NEFT',  'Salary payout Jul',              b'0', 'COMPLETED', NOW(6), 'INR', 1,  1),
(22000.00, NOW(6), NULL, NULL, 'IDEMP-0002', 'RTGS',  'Vendor payment',                 b'0', 'COMPLETED', NOW(6), 'INR', 2,  2),
(1800.00,  NOW(6), NULL, NULL, 'IDEMP-0003', 'UPI',   'Utility bill',                   b'0', 'VALIDATED', NOW(6), 'INR', 3,  3),
(54000.00, NOW(6), NULL, NULL, 'IDEMP-0004', 'RTGS',  'Equipment purchase',             b'0', 'SENT',      NOW(6), 'INR', 4,  4),
(7600.00,  NOW(6), 'BANK_TIMEOUT', 'Bank timeout', 'IDEMP-0005', 'NEFT', 'Subscription', b'0', 'FAILED',    NOW(6), 'INR', 5,  5),
(9100.00,  NOW(6), NULL, NULL, 'IDEMP-0006', 'UPI',   'Office expenses',                b'0', 'COMPLETED', NOW(6), 'INR', 6,  6),
(13300.00, NOW(6), NULL, NULL, 'IDEMP-0007', 'NEFT',  'Training cost',                  b'1', 'CREATED',   NOW(6), 'INR', 7,  7),
(28500.00, NOW(6), NULL, NULL, 'IDEMP-0008', 'RTGS',  'Rent transfer',                  b'0', 'COMPLETED', NOW(6), 'INR', 8,  8),
(6200.00,  NOW(6), 'LIMIT_EXCEEDED', 'Daily limit exceeded', 'IDEMP-0009', 'UPI', 'Insurance premium', b'0', 'FAILED', NOW(6), 'INR', 9,  9),
(4500.00,  NOW(6), NULL, NULL, 'IDEMP-0010', 'NEFT',  'Reimbursement',                  b'0', 'COMPLETED', NOW(6), 'INR', 10, 10),

(1200.00,  NOW(6), NULL, NULL, 'IDEMP-0011', 'WIRE',  'Consulting fee',                 b'0', 'COMPLETED', NOW(6), 'USD', 11, 11),
(950.00,   NOW(6), NULL, NULL, 'IDEMP-0012', 'ACH',   'Retail supplier',                b'0', 'SENT',      NOW(6), 'USD', 12, 12),
(780.00,   NOW(6), NULL, NULL, 'IDEMP-0013', 'FPS',   'Software license',               b'0', 'COMPLETED', NOW(6), 'GBP', 13, 13),
(640.00,   NOW(6), 'INVALID_ACC', 'Receiver validation failed', 'IDEMP-0014', 'BACS', 'Medical supplies', b'0', 'FAILED', NOW(6), 'GBP', 14, 14),
(1100.00,  NOW(6), NULL, NULL, 'IDEMP-0015', 'SEPA',  'Mobility parts',                 b'0', 'COMPLETED', NOW(6), 'EUR', 15, 15),
(520.00,   NOW(6), NULL, NULL, 'IDEMP-0016', 'SEPA',  'Food imports',                   b'1', 'CREATED',   NOW(6), 'EUR', 16, 16),
(2100.00,  NOW(6), NULL, NULL, 'IDEMP-0017', 'FAST',  'Investment transfer',            b'0', 'COMPLETED', NOW(6), 'SGD', 17, 17),
(3400.00,  NOW(6), NULL, NULL, 'IDEMP-0018', 'WPS',   'Payroll cycle',                  b'0', 'COMPLETED', NOW(6), 'AED', 18, 18),
(870.00,   NOW(6), 'COMPLIANCE_HOLD', 'Compliance hold', 'IDEMP-0019', 'NPP', 'Data services', b'0', 'FAILED', NOW(6), 'USD', 19, 19),
(980.00,   NOW(6), NULL, NULL, 'IDEMP-0020', 'ZENGIN','Precision tools',                b'0', 'VALIDATED', NOW(6), 'USD', 20, 20);

-- =========================================================
-- 6) PAYMENT_STATUS_HISTORY
-- =========================================================
INSERT INTO payment_status_history
(changed_at, new_status, old_status, remarks, payment_id)
VALUES
(NOW(6), 'COMPLETED', 'CREATED',   'Processed successfully',         1),
(NOW(6), 'COMPLETED', 'SENT',      'Settled by bank',                2),
(NOW(6), 'VALIDATED', 'CREATED',   'Validation completed',           3),
(NOW(6), 'SENT',      'VALIDATED', 'Sent to bank switch',            4),
(NOW(6), 'FAILED',    'SENT',      'Timeout at gateway',             5),
(NOW(6), 'COMPLETED', 'VALIDATED', 'UPI success',                    6),
(NOW(6), 'CREATED',   'CREATED',   'Scheduled for future execution', 7),
(NOW(6), 'COMPLETED', 'SENT',      'RTGS settled',                   8),
(NOW(6), 'FAILED',    'VALIDATED', 'Transfer limit exceeded',        9),
(NOW(6), 'COMPLETED', 'SENT',      'NEFT settled',                   10),
(NOW(6), 'COMPLETED', 'SENT',      'Wire success',                   11),
(NOW(6), 'SENT',      'VALIDATED', 'In clearing pipeline',           12),
(NOW(6), 'COMPLETED', 'SENT',      'FPS success',                    13),
(NOW(6), 'FAILED',    'VALIDATED', 'Invalid beneficiary',            14),
(NOW(6), 'COMPLETED', 'SENT',      'SEPA success',                   15),
(NOW(6), 'CREATED',   'CREATED',   'Recurring schedule created',     16),
(NOW(6), 'COMPLETED', 'SENT',      'FAST transfer success',          17),
(NOW(6), 'COMPLETED', 'SENT',      'Payroll transfer success',       18),
(NOW(6), 'FAILED',    'SENT',      'Compliance screening failed',    19),
(NOW(6), 'VALIDATED', 'CREATED',   'Awaiting bank settlement',       20);

-- =========================================================
-- 7) SCHEDULED_PAYMENT (10 sample records)
-- frequency: MONTHLY | WEEKLY
-- =========================================================
INSERT INTO scheduled_payment
(amount, created_at, frequency, is_active, last_execution_date, next_execution_date, currency_code, receiver_id, sender_account_id)
VALUES
(13300.00, NOW(6), 'MONTHLY', b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'INR', 7, 7),
(520.00,   NOW(6), 'MONTHLY', b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'EUR', 16,16),
(1200.00,  NOW(6), 'WEEKLY',  b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY),  'USD', 11,11),
(3400.00,  NOW(6), 'MONTHLY', b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'AED', 18,18),
(9100.00,  NOW(6), 'WEEKLY',  b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY),  'INR', 6, 6),
(780.00,   NOW(6), 'MONTHLY', b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'GBP', 13,13),
(1100.00,  NOW(6), 'MONTHLY', b'0', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'EUR', 15,15),
(2100.00,  NOW(6), 'WEEKLY',  b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY),  'SGD', 17,17),
(4500.00,  NOW(6), 'MONTHLY', b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'INR', 10,10),
(950.00,   NOW(6), 'WEEKLY',  b'1', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY),  'USD', 12,12);

-- =========================================================
-- 8) REFUND (5 sample records)
-- refund_status: APPROVED | COMPLETED | REJECTED | REQUESTED
-- =========================================================
INSERT INTO refund
(completed_at, reason, refund_amount, refund_status, requested_at, payment_id)
VALUES
(NOW(6), 'Failed transfer reversal',               7600.00, 'COMPLETED', NOW(6), 5),
(NOW(6), 'Limit exceeded reversal',                6200.00, 'COMPLETED', NOW(6), 9),
(NOW(6), 'Invalid beneficiary reversal',            640.00, 'COMPLETED', NOW(6), 14),
(NULL,   'Customer requested cancellation',        1200.00, 'REQUESTED', NOW(6), 11),
(NULL,   'Compliance review refund',                870.00, 'APPROVED',  NOW(6), 19);


SHOW COLUMNS FROM currency;

-- Total rows
SELECT COUNT(*) AS total_currencies
FROM currency;

-- View all inserted rows
SELECT *
FROM currency
ORDER BY currency_code;

-- Total rows
SELECT COUNT(*) AS total_banks
FROM bank;

-- India vs offshore split
SELECT
  CASE WHEN country_code = 'IND' THEN 'INDIA' ELSE 'OFFSHORE' END AS region,
  COUNT(*) AS total
FROM bank
GROUP BY CASE WHEN country_code = 'IND' THEN 'INDIA' ELSE 'OFFSHORE' END;

-- Detail view
SELECT bank_id, bank_name, swift_code, country_code, created_at
FROM bank
ORDER BY bank_id;