package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.CurrencyRequest;
import payment.processing.system.DTO.Response.CurrencyResponse;
import payment.processing.system.Exception.DuplicateResourceException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Currency;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Repository.CurrencyRepository;
import payment.processing.system.Services.CurrencyService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public CurrencyResponse createCurrency(CurrencyRequest request) {
        if (currencyRepository.existsById(request.getCurrencyCode())) {
            throw new DuplicateResourceException("Currency already exists: " + request.getCurrencyCode());
        }
        Currency currency = Currency.builder()
                .currencyCode(request.getCurrencyCode())
                .currencyName(request.getCurrencyName())
                .currencySymbol(request.getCurrencySymbol())
                .isSupported(request.getIsSupported() == null ? true : request.getIsSupported())
                .build();
        return toResponse(currencyRepository.save(currency));
    }

    @Override
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CurrencyResponse getCurrencyByCode(CurrencyCode code) {
        return toResponse(findByCode(code));
    }

    @Override
    public Currency getEntityByCode(CurrencyCode code) {
        return findByCode(code);
    }

    private Currency findByCode(CurrencyCode code) {
        return currencyRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + code));
    }

    private CurrencyResponse toResponse(Currency currency) {
        return CurrencyResponse.builder()
                .currencyCode(currency.getCurrencyCode())
                .currencyName(currency.getCurrencyName())
                .currencySymbol(currency.getCurrencySymbol())
                .isSupported(currency.getIsSupported())
                .build();
    }
}

