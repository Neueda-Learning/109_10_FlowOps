package payment.processing.system.Services;

import payment.processing.system.DTO.Request.BankRequest;
import payment.processing.system.DTO.Response.BankResponse;
import payment.processing.system.Model.Bank;

import java.util.List;

public interface BankService {

    BankResponse createBank(BankRequest request);

    List<BankResponse> getAllBanks();

    BankResponse getBankById(Long bankId);

    Bank getEntityById(Long bankId);
}

