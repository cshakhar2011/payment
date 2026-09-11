package com.payment.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkRequest {
    @NotNull(message = "amount is required")
    @Min(value = 100, message = "amount must be at least 100 smallest currency units")
    private Long amount;
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must contain exactly 3 letters")
    private String currency;
    @JsonProperty("upi_link")
    private Boolean upiLink;
    @JsonProperty("accept_partial")
    private boolean acceptPartial;
    @JsonProperty("first_min_partial_amount")
    @Min(value = 100, message = "firstMinPartialAmount must be at least 100 smallest currency units")
    private Long firstMinPartialAmount;
    @JsonProperty("expire_by")
    private Long expireBy;
    @JsonProperty("reference_id")
    private String referenceId;
    @NotBlank(message = "description is required")
    private String description;
    @Valid
    @NotNull(message = "customer is required")
    private Customer customer;
    @JsonProperty("notify")
    @Valid
    private Notify notification;
    @JsonProperty("reminder_enable")
    private boolean reminderEnable;
    private Map<String, Object> notes;
    @JsonProperty("callback_url")
    private String callbackUrl;
    @JsonProperty("callback_method")
    @Pattern(regexp = "get|post", message = "callbackMethod must be get or post")
    private String callbackMethod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private String name;
        private String contact;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notify {
        private Boolean sms;
        private Boolean email;
    }
}