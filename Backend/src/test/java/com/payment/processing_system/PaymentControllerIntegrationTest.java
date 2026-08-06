package com.payment.processing_system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessPaymentFromCreatedToCompleted() throws Exception {
        String createBody = """
                {
                  \"paymentReference\": \"PAY-IND-1001\",
                  \"paymentType\": \"INDIVIDUAL_PAYMENT\",
                  \"amount\": 150.50,
                  \"currency\": \"usd\",
                  \"recipientName\": \"John Doe\",
                  \"recipientAccount\": \"ACC-1001\"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.errorCode").isEmpty())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long paymentId = jsonNode.get("id").asLong();

        mockMvc.perform(post("/api/payments/{id}/validate", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldFailWhenTransitionOrderIsInvalid() throws Exception {
        String createBody = """
                {
                  \"paymentReference\": \"PAY-ENT-1002\",
                  \"paymentType\": \"ENTITY_PAYMENT\",
                  \"amount\": 240.00,
                  \"currency\": \"EUR\",
                  \"recipientName\": \"Acme Corp\",
                  \"recipientAccount\": \"ENT-22\"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long paymentId = jsonNode.get("id").asLong();

        mockMvc.perform(post("/api/payments/{id}/complete", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnHistoryAndAllowRefundForCompletedPayment() throws Exception {
        String createBody = """
                {
                  \"paymentReference\": \"PAY-HISTORY-3001\",
                  \"paymentType\": \"INTERNATIONAL_PAYMENT\",
                  \"amount\": 500.00,
                  \"currency\": \"USD\",
                  \"recipientName\": \"Global Vendor\",
                  \"recipientAccount\": \"INT-555\"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long paymentId = jsonNode.get("id").asLong();

        mockMvc.perform(get("/api/payments/history")
                        .param("paymentType", "INTERNATIONAL_PAYMENT")
                        .param("status", "COMPLETED")
                        .param("paymentReference", "HISTORY-3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(paymentId));

        mockMvc.perform(post("/api/payments/{id}/refund", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        mockMvc.perform(get("/api/payments/history")
                        .param("paymentReference", "PAY-HISTORY-3001")
                        .param("status", "REFUNDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(paymentId))
                .andExpect(jsonPath("$[0].status").value("REFUNDED"));
    }
}





