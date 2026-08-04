package payment.processing.system.Services.Impl;

import payment.processing.system.DTO.Request.AccountRequest;
import payment.processing.system.DTO.Response.AccountResponse;
import payment.processing.system.Model.Account;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    List<AccountResponse> getAllAccounts();

    AccountResponse getAccountById(Long accountId);

    Account getEntityById(Long accountId);
}

