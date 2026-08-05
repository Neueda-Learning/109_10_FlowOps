package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.system.DTO.Request.BankRequest;
import payment.processing.system.DTO.Response.BankResponse;
import payment.processing.system.Services.BankService;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
@Tag(name = "Bank", description = "Bank management APIs")
public class BankController {

    private final BankService bankService;

    @PostMapping
    @Operation(summary = "Register a new bank")
    public ResponseEntity<BankResponse> createBank(@Valid @RequestBody BankRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createBank(request));
    }

    @GetMapping
    @Operation(summary = "List all banks")
    public ResponseEntity<List<BankResponse>> getAllBanks() {
        return ResponseEntity.ok(bankService.getAllBanks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank by id")
    public ResponseEntity<BankResponse> getBank(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getBankById(id));
    }
}


