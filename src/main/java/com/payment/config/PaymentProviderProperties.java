package com.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.provider")
public record PaymentProviderProperties(
        String name,
        String keyId,
        String keySecret,
        String webhookSecret) {
}