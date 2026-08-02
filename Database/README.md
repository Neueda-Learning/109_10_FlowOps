# FlowOps Payment Processing Database


## Overview

This database supports the FlowOps corporate payment processing system.

The system allows a single employer to process salary payments to employees across different banks and currencies.


## Tables


### bank

Stores supported banks.


### currency

Stores supported currencies.


### account

Stores company and employee bank accounts.


### receiver

Stores employees receiving payments.


### payment

Stores payment transactions and lifecycle status.


### payment_status_history

Stores audit history of payment status changes.


### scheduled_payment

Stores future recurring salary payments.


### refund

Stores refund requests and processing status.



## Payment Lifecycle


CREATED

↓

VALIDATED

↓

SENT

↓

COMPLETED



FAILED can occur at any stage.



## Technologies

Database:

MySQL


ER Diagram:

dbdiagram.io