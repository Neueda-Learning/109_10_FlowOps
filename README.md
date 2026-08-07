# Payment Processing System - Unit Test Cases

## Unit Testing

The following unit test cases are created to verify the important functionalities of the Payment Processing System.  
These tests validate payment processing, retrieval, filtering, refund handling, bulk payment creation, and dashboard summary generation.

| Test Case ID | Test Scenario | Description | Expected Result |
|--------------|---------------|-------------|-----------------|
| 1 | Payment Completed Successfully | Verify that a SENT payment can be completed successfully. | Payment status should change to COMPLETED. |
| 2 | Create Payment With Invalid Amount | Verify that payment creation rejects invalid amount values. | Payment should not be created and validation error should occur. |
| 3 | Get Payment By Valid ID | Verify retrieving payment details using an existing payment ID. | Correct payment details should be returned. |
| 4 | List All Payments | Verify retrieving all available payments. | All payment records should be displayed. |
| 5 | Filter Payments By Payment Type | Verify filtering payments based on payment type. | Only matching payment type records should be returned. |
| 6 | Refund Completed Payment | Verify refund processing for a completed payment. | Status should become REFUNDED. |
| 7 | Bulk Payment Creation | Verify creation of multiple payments in a single request. | All payments should be processed. |
| 8 | Get Dashboard Summary | Verify dashboard summary generation using existing payments. | Summary should return successfully. |