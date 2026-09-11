package com.payment.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentOrderRequest {
    @NotNull(message = "customerId is required")
    private Long customerId;
    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.00", message = "amount must be at least 1.00")
    @Digits(integer = 17, fraction = 2, message = "amount must have no more than 2 decimal places")
    private BigDecimal amount;
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must contain exactly 3 letters")
    private String currency;
    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;
}
