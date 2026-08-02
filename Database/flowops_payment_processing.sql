-- =====================================================
-- FLOWOPS PAYMENT PROCESSING SYSTEM
-- DATABASE CREATION
-- =====================================================

DROP DATABASE IF EXISTS flowops_payment_processing;

CREATE DATABASE flowops_payment_processing;

USE flowops_payment_processing;


-- =====================================================
-- TABLE 1: BANK
-- Stores supported banks
-- =====================================================

CREATE TABLE bank (

    bank_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    bank_name VARCHAR(100) NOT NULL,

    swift_code VARCHAR(20) UNIQUE NOT NULL,

    country_code CHAR(3) NOT NULL,

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



-- =====================================================
-- TABLE 2: CURRENCY
-- Stores supported currencies
-- =====================================================

CREATE TABLE currency (

    currency_code CHAR(3) PRIMARY KEY,

    currency_name VARCHAR(50) NOT NULL,

    currency_symbol VARCHAR(5),

    is_supported BOOLEAN DEFAULT TRUE

);



-- =====================================================
-- TABLE 3: ACCOUNT
-- Stores company and employee bank accounts
-- =====================================================

CREATE TABLE account (

    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    bank_id BIGINT NOT NULL,

    currency_code CHAR(3) NOT NULL,

    account_number VARCHAR(50) UNIQUE NOT NULL,

    account_holder_name VARCHAR(100) NOT NULL,

    account_type ENUM(
        'BUSINESS',
        'PERSONAL'
    ) NOT NULL,

    country_code CHAR(3) NOT NULL,

    balance DECIMAL(15,2) DEFAULT 0,

    status ENUM(
        'ACTIVE',
        'BLOCKED'
    ) DEFAULT 'ACTIVE',


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_account_bank
        FOREIGN KEY (bank_id)
        REFERENCES bank(bank_id),


    CONSTRAINT fk_account_currency
        FOREIGN KEY (currency_code)
        REFERENCES currency(currency_code)

);



-- =====================================================
-- TABLE 4: RECEIVER
-- Stores employees receiving salary payments
-- =====================================================

CREATE TABLE receiver (

    receiver_id BIGINT AUTO_INCREMENT PRIMARY KEY,


    account_id BIGINT UNIQUE NOT NULL,


    employee_id VARCHAR(20) UNIQUE NOT NULL,


    full_name VARCHAR(100) NOT NULL,


    email VARCHAR(100),


    department VARCHAR(100),


    employment_status ENUM(
        'ACTIVE',
        'INACTIVE'
    ) DEFAULT 'ACTIVE',


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_receiver_account

        FOREIGN KEY(account_id)

        REFERENCES account(account_id)

);



-- =====================================================
-- TABLE 5: PAYMENT
-- Main payment transaction table
-- =====================================================

CREATE TABLE payment (

    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,


    sender_account_id BIGINT NOT NULL,


    receiver_id BIGINT NOT NULL,


    amount DECIMAL(15,2) NOT NULL,


    currency_code CHAR(3) NOT NULL,


    payment_type ENUM(
        'SALARY'
    ) DEFAULT 'SALARY',


    status ENUM(

        'CREATED',

        'VALIDATED',

        'SENT',

        'COMPLETED',

        'FAILED'

    ) DEFAULT 'CREATED',


    idempotency_key VARCHAR(100) UNIQUE NOT NULL,


    reference VARCHAR(255),


    error_code VARCHAR(50),


    error_message VARCHAR(255),


    scheduled BOOLEAN DEFAULT FALSE,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

        ON UPDATE CURRENT_TIMESTAMP,



    CONSTRAINT fk_payment_sender

        FOREIGN KEY(sender_account_id)

        REFERENCES account(account_id),



    CONSTRAINT fk_payment_receiver

        FOREIGN KEY(receiver_id)

        REFERENCES receiver(receiver_id),



    CONSTRAINT fk_payment_currency

        FOREIGN KEY(currency_code)

        REFERENCES currency(currency_code)

);



-- =====================================================
-- TABLE 6: PAYMENT STATUS HISTORY
-- Audit trail of payment lifecycle
-- =====================================================

CREATE TABLE payment_status_history (

    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,


    payment_id BIGINT NOT NULL,


    old_status VARCHAR(20),


    new_status VARCHAR(20) NOT NULL,


    remarks VARCHAR(255),


    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,



    CONSTRAINT fk_history_payment

        FOREIGN KEY(payment_id)

        REFERENCES payment(payment_id)

);



-- =====================================================
-- TABLE 7: SCHEDULED PAYMENT
-- Future recurring salary payments
-- =====================================================

CREATE TABLE scheduled_payment (

    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,


    sender_account_id BIGINT NOT NULL,


    receiver_id BIGINT NOT NULL,


    amount DECIMAL(15,2) NOT NULL,


    currency_code CHAR(3) NOT NULL,


    frequency ENUM(

        'WEEKLY',

        'MONTHLY'

    ) NOT NULL,


    next_execution_date DATE NOT NULL,


    last_execution_date DATE,


    is_active BOOLEAN DEFAULT TRUE,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,



    CONSTRAINT fk_schedule_sender

        FOREIGN KEY(sender_account_id)

        REFERENCES account(account_id),



    CONSTRAINT fk_schedule_receiver

        FOREIGN KEY(receiver_id)

        REFERENCES receiver(receiver_id),



    CONSTRAINT fk_schedule_currency

        FOREIGN KEY(currency_code)

        REFERENCES currency(currency_code)

);



-- =====================================================
-- TABLE 8: REFUND
-- Refund processing
-- =====================================================

CREATE TABLE refund (

    refund_id BIGINT AUTO_INCREMENT PRIMARY KEY,


    payment_id BIGINT NOT NULL,


    refund_amount DECIMAL(15,2) NOT NULL,


    reason VARCHAR(255) NOT NULL,


    refund_status ENUM(

        'REQUESTED',

        'APPROVED',

        'COMPLETED',

        'REJECTED'

    ) DEFAULT 'REQUESTED',


    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    completed_at TIMESTAMP NULL,



    CONSTRAINT fk_refund_payment

        FOREIGN KEY(payment_id)

        REFERENCES payment(payment_id)

);

SHOW TABLES;
SELECT DATABASE();

-- =====================================================
-- PART 2
-- MASTER DATA INSERTION
-- =====================================================


-- =====================================================
-- INSERT BANKS
-- =====================================================

INSERT INTO bank
(
    bank_name,
    swift_code,
    country_code
)
VALUES

('Bank of Ireland',
 'BOFIIE2D',
 'IRL'),


('AIB Bank',
 'AIBKIE2D',
 'IRL'),


('HSBC UK',
 'MIDLGB22',
 'GBR'),


('ICICI Bank',
 'ICICINBB',
 'IND'),


('Chase Bank',
 'CHASUS33',
 'USA');



-- =====================================================
-- INSERT SUPPORTED CURRENCIES
-- =====================================================

INSERT INTO currency
(
    currency_code,
    currency_name,
    currency_symbol
)
VALUES

('EUR',
 'Euro',
 '€'),


('GBP',
 'British Pound',
 '£'),


('USD',
 'United States Dollar',
 '$'),


('INR',
 'Indian Rupee',
 '₹');



-- =====================================================
-- INSERT COMPANY ACCOUNT
-- FlowOps Ltd Business Account
-- =====================================================

INSERT INTO account
(
    bank_id,
    currency_code,
    account_number,
    account_holder_name,
    account_type,
    country_code,
    balance
)
VALUES

(
    1,
    'EUR',
    'IE-FLOWOPS-001',
    'FlowOps Ltd',
    'BUSINESS',
    'IRL',
    500000.00
);

SELECT * FROM bank;
SELECT * FROM currency;
SELECT * FROM account;

-- =====================================================
-- PART 3
-- EMPLOYEE ACCOUNTS
-- =====================================================


INSERT INTO account
(
    bank_id,
    currency_code,
    account_number,
    account_holder_name,
    account_type,
    country_code,
    balance
)
VALUES


-- =====================================================
-- IRELAND EMPLOYEES
-- Bank of Ireland
-- =====================================================

(1,'EUR','IE-EMP-001','John Murphy','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-002','Sarah Wilson','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-003','Michael Kelly','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-004','Emma Ryan','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-005','Daniel Byrne','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-006','Laura Doyle','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-007','James Nolan','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-008','Aoife Walsh','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-009','Patrick Kelly','PERSONAL','IRL',0),

(1,'EUR','IE-EMP-010','Niamh OConnor','PERSONAL','IRL',0),



-- =====================================================
-- IRELAND EMPLOYEES
-- AIB Bank
-- =====================================================

(2,'EUR','IE-AIB-001','Brian Doyle','PERSONAL','IRL',0),

(2,'EUR','IE-AIB-002','Rachel Byrne','PERSONAL','IRL',0),

(2,'EUR','IE-AIB-003','Conor Ryan','PERSONAL','IRL',0),

(2,'EUR','IE-AIB-004','Lisa Murphy','PERSONAL','IRL',0),

(2,'EUR','IE-AIB-005','Kevin Walsh','PERSONAL','IRL',0),



-- =====================================================
-- UK EMPLOYEES
-- HSBC
-- =====================================================

(3,'GBP','UK-HSBC-001','Oliver Smith','PERSONAL','GBR',0),

(3,'GBP','UK-HSBC-002','Emily Brown','PERSONAL','GBR',0),

(3,'GBP','UK-HSBC-003','George Taylor','PERSONAL','GBR',0),

(3,'GBP','UK-HSBC-004','Sophie Johnson','PERSONAL','GBR',0),

(3,'GBP','UK-HSBC-005','Harry Wilson','PERSONAL','GBR',0),



-- =====================================================
-- INDIA EMPLOYEES
-- ICICI
-- =====================================================

(4,'INR','IN-ICICI-001','Raj Patel','PERSONAL','IND',0),

(4,'INR','IN-ICICI-002','Priya Sharma','PERSONAL','IND',0),

(4,'INR','IN-ICICI-003','Amit Kumar','PERSONAL','IND',0),

(4,'INR','IN-ICICI-004','Neha Singh','PERSONAL','IND',0),

(4,'INR','IN-ICICI-005','Vikram Shah','PERSONAL','IND',0),



-- =====================================================
-- USA EMPLOYEES
-- Chase Bank
-- =====================================================

(5,'USD','US-CHASE-001','John Carter','PERSONAL','USA',0),

(5,'USD','US-CHASE-002','Anna Davis','PERSONAL','USA',0),

(5,'USD','US-CHASE-003','Robert Miller','PERSONAL','USA',0),

(5,'USD','US-CHASE-004','Jessica Moore','PERSONAL','USA',0),

(5,'USD','US-CHASE-005','William Anderson','PERSONAL','USA',0);

-- =====================================================
-- RECEIVER DATA
-- =====================================================


INSERT INTO receiver
(
    account_id,
    employee_id,
    full_name,
    email,
    department
)
VALUES

(2,'EMP001','John Murphy','john.murphy@flowops.com','Finance'),

(3,'EMP002','Sarah Wilson','sarah.wilson@flowops.com','Technology'),

(4,'EMP003','Michael Kelly','michael.kelly@flowops.com','Engineering'),

(5,'EMP004','Emma Ryan','emma.ryan@flowops.com','HR'),

(6,'EMP005','Daniel Byrne','daniel.byrne@flowops.com','Finance'),

(7,'EMP006','Laura Doyle','laura.doyle@flowops.com','Technology'),

(8,'EMP007','James Nolan','james.nolan@flowops.com','Engineering'),

(9,'EMP008','Aoife Walsh','aoife.walsh@flowops.com','HR'),

(10,'EMP009','Patrick Kelly','patrick.kelly@flowops.com','Finance'),

(11,'EMP010','Niamh OConnor','niamh.oconnor@flowops.com','Technology'),


(12,'EMP011','Brian Doyle','brian.doyle@flowops.com','Engineering'),

(13,'EMP012','Rachel Byrne','rachel.byrne@flowops.com','Finance'),

(14,'EMP013','Conor Ryan','conor.ryan@flowops.com','HR'),

(15,'EMP014','Lisa Murphy','lisa.murphy@flowops.com','Technology'),

(16,'EMP015','Kevin Walsh','kevin.walsh@flowops.com','Finance'),


(17,'EMP016','Oliver Smith','oliver.smith@flowops.com','Engineering'),

(18,'EMP017','Emily Brown','emily.brown@flowops.com','Finance'),

(19,'EMP018','George Taylor','george.taylor@flowops.com','Technology'),

(20,'EMP019','Sophie Johnson','sophie.johnson@flowops.com','HR'),

(21,'EMP020','Harry Wilson','harry.wilson@flowops.com','Finance'),


(22,'EMP021','Raj Patel','raj.patel@flowops.com','Engineering'),

(23,'EMP022','Priya Sharma','priya.sharma@flowops.com','Technology'),

(24,'EMP023','Amit Kumar','amit.kumar@flowops.com','Finance'),

(25,'EMP024','Neha Singh','neha.singh@flowops.com','HR'),

(26,'EMP025','Vikram Shah','vikram.shah@flowops.com','Engineering'),


(27,'EMP026','John Carter','john.carter@flowops.com','Finance'),

(28,'EMP027','Anna Davis','anna.davis@flowops.com','Technology'),

(29,'EMP028','Robert Miller','robert.miller@flowops.com','Engineering'),

(30,'EMP029','Jessica Moore','jessica.moore@flowops.com','HR'),

(31,'EMP030','William Anderson','william.anderson@flowops.com','Finance');

SELECT * FROM account;
SELECT * FROM receiver;
SELECT 
r.full_name,
a.account_number,
b.bank_name,
a.currency_code

FROM receiver r

JOIN account a
ON r.account_id = a.account_id

JOIN bank b
ON a.bank_id = b.bank_id;

-- =====================================================
-- PART 4
-- PAYMENT TRANSACTIONS
-- =====================================================


INSERT INTO payment
(
    sender_account_id,
    receiver_id,
    amount,
    currency_code,
    payment_type,
    status,
    idempotency_key,
    reference
)
VALUES


-- =====================================================
-- COMPLETED EUR SALARY PAYMENTS
-- =====================================================


(1,1,5000.00,'EUR','SALARY','COMPLETED',
'PAY-2026-001',
'January salary payment'),


(1,2,5200.00,'EUR','SALARY','COMPLETED',
'PAY-2026-002',
'January salary payment'),


(1,3,6000.00,'EUR','SALARY','COMPLETED',
'PAY-2026-003',
'January salary payment'),


(1,4,4500.00,'EUR','SALARY','COMPLETED',
'PAY-2026-004',
'January salary payment'),


(1,5,4800.00,'EUR','SALARY','COMPLETED',
'PAY-2026-005',
'January salary payment'),



-- =====================================================
-- AIB EMPLOYEES
-- =====================================================


(1,11,5300.00,'EUR','SALARY','COMPLETED',
'PAY-2026-006',
'January salary payment'),


(1,12,4700.00,'EUR','SALARY','COMPLETED',
'PAY-2026-007',
'January salary payment'),


(1,13,5100.00,'EUR','SALARY','COMPLETED',
'PAY-2026-008',
'January salary payment'),



-- =====================================================
-- UK SALARIES
-- =====================================================


(1,16,4000.00,'GBP','SALARY','COMPLETED',
'PAY-2026-009',
'UK employee salary'),


(1,17,4300.00,'GBP','SALARY','COMPLETED',
'PAY-2026-010',
'UK employee salary'),


(1,18,3900.00,'GBP','SALARY','COMPLETED',
'PAY-2026-011',
'UK employee salary'),



-- =====================================================
-- INDIA SALARIES
-- =====================================================


(1,21,120000.00,'INR','SALARY','COMPLETED',
'PAY-2026-012',
'India employee salary'),


(1,22,100000.00,'INR','SALARY','COMPLETED',
'PAY-2026-013',
'India employee salary'),


(1,23,90000.00,'INR','SALARY','COMPLETED',
'PAY-2026-014',
'India employee salary'),



-- =====================================================
-- USA SALARIES
-- =====================================================


(1,26,7000.00,'USD','SALARY','COMPLETED',
'PAY-2026-015',
'US employee salary'),


(1,27,6500.00,'USD','SALARY','COMPLETED',
'PAY-2026-016',
'US employee salary'),



-- =====================================================
-- FAILED PAYMENTS
-- =====================================================


(1,28,6000.00,'USD','SALARY','FAILED',
'PAY-2026-017',
'Invalid receiver account'),


(1,29,5500.00,'USD','SALARY','FAILED',
'PAY-2026-018',
'Insufficient funds simulation'),



-- =====================================================
-- PROCESSING PAYMENTS
-- =====================================================


(1,30,5800.00,'USD','SALARY','SENT',
'PAY-2026-019',
'Waiting bank confirmation');

-- =====================================================
-- PAYMENT STATUS HISTORY
-- =====================================================


INSERT INTO payment_status_history
(
payment_id,
old_status,
new_status,
remarks
)
VALUES


-- Payment 1 lifecycle

(1,NULL,'CREATED',
'Payment submitted'),


(1,'CREATED','VALIDATED',
'Validation successful'),


(1,'VALIDATED','SENT',
'Sent to bank'),


(1,'SENT','COMPLETED',
'Payment completed'),



-- Payment 2 lifecycle

(2,NULL,'CREATED',
'Payment submitted'),


(2,'CREATED','VALIDATED',
'Validation successful'),


(2,'VALIDATED','SENT',
'Sent to bank'),


(2,'SENT','COMPLETED',
'Payment completed'),



-- Payment 17 failed

(17,NULL,'CREATED',
'Payment submitted'),


(17,'CREATED','FAILED',
'INVALID_ACCOUNT'),



-- Payment 18 failed

(18,NULL,'CREATED',
'Payment submitted'),


(18,'CREATED','VALIDATED',
'Validation successful'),


(18,'VALIDATED','FAILED',
'PROCESSING_ERROR'),



-- Payment 19 still processing

(19,NULL,'CREATED',
'Payment submitted'),


(19,'CREATED','VALIDATED',
'Validation successful'),


(19,'VALIDATED','SENT',
'Awaiting bank confirmation');

SELECT * FROM payment;
SELECT
p.payment_id,
p.status,
h.old_status,
h.new_status,
h.remarks

FROM payment p

JOIN payment_status_history h

ON p.payment_id=h.payment_id

ORDER BY p.payment_id;

-- =====================================================
-- PART 5
-- SCHEDULED PAYMENTS
-- =====================================================


INSERT INTO scheduled_payment
(
    sender_account_id,
    receiver_id,
    amount,
    currency_code,
    frequency,
    next_execution_date
)
VALUES


-- Ireland monthly salaries

(1,1,5000.00,'EUR',
'MONTHLY',
'2026-02-01'),


(1,2,5200.00,'EUR',
'MONTHLY',
'2026-02-01'),


(1,3,6000.00,'EUR',
'MONTHLY',
'2026-02-01'),



-- UK monthly salaries

(1,16,4000.00,'GBP',
'MONTHLY',
'2026-02-01'),


(1,17,4300.00,'GBP',
'MONTHLY',
'2026-02-01'),



-- India monthly salaries

(1,21,120000.00,'INR',
'MONTHLY',
'2026-02-01'),


(1,22,100000.00,'INR',
'MONTHLY',
'2026-02-01'),



-- USA monthly salaries

(1,26,7000.00,'USD',
'MONTHLY',
'2026-02-01'),


(1,27,6500.00,'USD',
'MONTHLY',
'2026-02-01');

SELECT * FROM scheduled_payment;

-- =====================================================
-- REFUND DATA
-- =====================================================


INSERT INTO refund
(
    payment_id,
    refund_amount,
    reason,
    refund_status,
    completed_at
)
VALUES


(
1,
5000.00,
'DUPLICATE_PAYMENT',
'COMPLETED',
'2026-01-15 10:30:00'
),


(
2,
5200.00,
'WRONG_AMOUNT',
'COMPLETED',
'2026-01-16 11:00:00'
),


(
3,
1000.00,
'PARTIAL_REFUND_REQUEST',
'APPROVED',
NULL
),


(
10,
4300.00,
'PAYMENT_ERROR',
'REQUESTED',
NULL
);

SELECT * FROM refund;
SELECT COUNT(*) AS total_payments
FROM payment;
SELECT 
SUM(amount) AS total_amount
FROM payment

WHERE status='COMPLETED';

SELECT

ROUND(

(
COUNT(CASE WHEN status='COMPLETED'
THEN 1 END)

/

COUNT(*) * 100

),2)

AS success_rate

FROM payment;

SELECT

COUNT(*) AS failed_payments

FROM payment

WHERE status='FAILED';

SELECT

currency_code,

COUNT(*) AS number_of_payments,

SUM(amount) AS total_amount


FROM payment


GROUP BY currency_code;

SELECT

b.bank_name,

COUNT(*) AS payments


FROM payment p


JOIN receiver r

ON p.receiver_id=r.receiver_id


JOIN account a

ON r.account_id=a.account_id


JOIN bank b

ON a.bank_id=b.bank_id


GROUP BY b.bank_name;

SELECT

refund_status,

COUNT(*) AS total

FROM refund

GROUP BY refund_status;