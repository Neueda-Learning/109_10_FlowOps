package payment.processing.system.Services.Impl;

import payment.processing.system.DTO.Request.CurrencyRequest;
import payment.processing.system.DTO.Response.CurrencyResponse;
import payment.processing.system.Model.Currency;
import payment.processing.system.Model.Enums.CurrencyCode;

import java.util.List;

public interface CurrencyService {

    CurrencyResponse createCurrency(CurrencyRequest request);

    List<CurrencyResponse> getAllCurrencies();

    CurrencyResponse getCurrencyByCode(CurrencyCode code);

    Currency getEntityByCode(CurrencyCode code);
}

