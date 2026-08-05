package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.system.DTO.Request.CurrencyRequest;
import payment.processing.system.DTO.Response.CurrencyResponse;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Services.CurrencyService;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency management APIs")
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    @Operation(summary = "Create a currency")
    public ResponseEntity<CurrencyResponse> createCurrency(@Valid @RequestBody CurrencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(currencyService.createCurrency(request));
    }

    @GetMapping
    @Operation(summary = "List all currencies")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get currency by code")
    public ResponseEntity<CurrencyResponse> getCurrency(@PathVariable("code") CurrencyCode code) {
        return ResponseEntity.ok(currencyService.getCurrencyByCode(code));
    }
}

