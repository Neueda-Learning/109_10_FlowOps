package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.project.DTO.Request.ReceiverRequest;
import payment.processing.project.DTO.Response.ReceiverResponse;
import payment.processing.project.Services.ReceiverService;

import java.util.List;

@RestController
@RequestMapping("/api/receivers")
@RequiredArgsConstructor
@Tag(name = "Receiver", description = "Employee (receiver) management APIs")
public class ReceiverController {

    private final ReceiverService receiverService;


    @PostMapping
    @Operation(summary = "Create a new receiver (employee)")
    public ResponseEntity<ReceiverResponse> createReceiver(@Valid @RequestBody ReceiverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiverService.createReceiver(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing receiver")
    public ResponseEntity<ReceiverResponse> updateReceiver(@PathVariable Long id, @Valid @RequestBody ReceiverRequest request) {
        return ResponseEntity.ok(receiverService.updateReceiver(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receiver by id")
    public ResponseEntity<ReceiverResponse> getReceiver(@PathVariable Long id) {
        return ResponseEntity.ok(receiverService.getReceiverById(id));
    }

    @GetMapping
    @Operation(summary = "List all receivers, optionally filtered by department")
    public ResponseEntity<List<ReceiverResponse>> getReceivers(
            @RequestParam(required = false) String department) {
        if (department != null) {
            return ResponseEntity.ok(receiverService.getReceiversByDepartment(department));
        }
        return ResponseEntity.ok(receiverService.getAllReceivers());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a receiver")
    public ResponseEntity<Void> deleteReceiver(@PathVariable Long id) {
        receiverService.deleteReceiver(id);
        return ResponseEntity.noContent().build();
    }
}

